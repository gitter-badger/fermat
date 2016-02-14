package com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1;

import com.bitdubai.fermat_api.CantStartPluginException;
import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.abstract_classes.AbstractPlugin;
import com.bitdubai.fermat_api.layer.all_definition.common.system.annotations.NeededAddonReference;
import com.bitdubai.fermat_api.layer.all_definition.common.system.utils.PluginVersionReference;
import com.bitdubai.fermat_api.layer.all_definition.enums.Addons;
import com.bitdubai.fermat_api.layer.all_definition.enums.Layers;
import com.bitdubai.fermat_api.layer.all_definition.enums.Platforms;
import com.bitdubai.fermat_api.layer.all_definition.enums.ServiceStatus;
import com.bitdubai.fermat_api.layer.all_definition.events.interfaces.FermatEventHandler;
import com.bitdubai.fermat_api.layer.all_definition.events.interfaces.FermatEventListener;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.Activity;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.Fragment;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.MainMenu;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.MenuItem;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.SideMenu;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.StatusBar;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.Tab;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.TabStrip;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.TitleBar;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.Wizard;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.WizardPage;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Activities;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Fragments;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.WizardPageTypes;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.WizardTypes;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.interfaces.FermatStructure;
import com.bitdubai.fermat_api.layer.all_definition.util.Version;
import com.bitdubai.fermat_api.layer.dmp_engine.sub_app_runtime.SubApp;
import com.bitdubai.fermat_api.layer.dmp_engine.sub_app_runtime.SubAppRuntimeManager;
import com.bitdubai.fermat_api.layer.dmp_engine.sub_app_runtime.enums.Apps;
import com.bitdubai.fermat_api.layer.dmp_engine.sub_app_runtime.enums.SubApps;
import com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.event_handlers.WalletResourcesInstalledEventHandler;
import com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.structure.RuntimeSubApp;
import com.bitdubai.fermat_pip_api.layer.platform_service.event_manager.enums.EventType;
import com.bitdubai.fermat_pip_api.layer.platform_service.event_manager.interfaces.EventManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The App Runtime is the module in charge of the UI navigation structure. A user is always at a certain point in this
 * structure.
 * <p/>
 * A Navigation stack is maintained by this plugin to allow the user to go back all the stack down to the root if necessary.
 * <p/>
 * Created by Matias Furszyfer 24/07/15
 */
public class SubAppRuntimeEnginePluginRoot extends AbstractPlugin implements SubAppRuntimeManager {

    /**
     * SubAppRuntimeManager Interface member variables.
     */
    List<FermatEventListener> listenersAdded = new ArrayList<>();
    /**
     * Map connect sub app public key - sub app
     */

    Map<String, SubApp> listSubApp = new HashMap<>();
    String lastSubapPublicKey;
    RuntimeSubApp homeScreen;
    @NeededAddonReference(platform = Platforms.PLUG_INS_PLATFORM, layer = Layers.PLATFORM_SERVICE, addon = Addons.EVENT_MANAGER)
    private EventManager eventManager;

    public SubAppRuntimeEnginePluginRoot() {
        super(new PluginVersionReference(new Version()));
    }

    @Override
    public void start() throws CantStartPluginException {
        try {
            /**
             * I will initialize the handling o
             *
             * f com.bitdubai.platform events.
             */
            FermatEventListener fermatEventListener;
            FermatEventHandler fermatEventHandler;
            fermatEventListener = eventManager.getNewListener(EventType.WALLET_RESOURCES_INSTALLED);
            fermatEventHandler = new WalletResourcesInstalledEventHandler(this);
            fermatEventListener.setEventHandler(fermatEventHandler);
            eventManager.addListener(fermatEventListener);
            listenersAdded.add(fermatEventListener);

            /**
             * At this time the only thing I can do is a factory reset. Once there should be a possibility to add
             * functionality based on wallets downloaded by users this wont be an option.
             * * *
             */
            factoryReset();

            this.serviceStatus = ServiceStatus.STARTED;
        } catch (com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.exceptions.CantFactoryResetException ex) {
            String message = CantStartPluginException.DEFAULT_MESSAGE;
            FermatException cause = ex;
            String context = "App Runtime Start";
            String possibleReason = "Some null definition";
            throw new CantStartPluginException(message, cause, context, possibleReason);
        } catch (Exception exception) {
            throw new CantStartPluginException(CantStartPluginException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, "Unchecked Exception occurred, check the cause");
        }
    }

    @Override
    public void stop() {
        /**
         * I will remove all the listeners registered with the event manager. 
         */
        for (FermatEventListener fermatEventListener : listenersAdded) {
            eventManager.removeListener(fermatEventListener);
        }

        listenersAdded.clear();

        this.serviceStatus = ServiceStatus.STOPPED;

    }

    /**
     * AppRuntime Interface implementation.
     */
    @Override
    public SubApp getLastApp() {
        if (lastSubapPublicKey != null) {
            return listSubApp.get(lastSubapPublicKey);
        }
        return homeScreen;
    }


    @Override
    public SubApp getSubAppByPublicKey(String subAppPublicKey) {
        SubApp subApp = listSubApp.get(subAppPublicKey);
        if (subApp != null) {
            lastSubapPublicKey = subApp.getPublicKey();
            return subApp;
        }
        //TODO METODO CON RETURN NULL - OJO: solo INFORMATIVO de ayuda VISUAL para DEBUG - Eliminar si molesta
        return null;
    }

    @Override
    public void recordNAvigationStructure(FermatStructure fermatStructure) {
        System.out.println("falta hacer esto");
    }


    /**
     * Here is where I actually generate the factory structure of the APP. This method is also useful to reset to the
     * factory structure.
     */
    private void factoryReset() throws com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.exceptions.CantFactoryResetException {

        try {
            com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.structure.RuntimeApp runtimeApp = new com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.structure.RuntimeApp();
            runtimeApp.setType(Apps.CRYPTO_WALLET_PLATFORM);

            RuntimeSubApp runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setPublicKey("public_key_shell");
            runtimeSubApp.setType(SubApps.CWP_SHELL);

            runtimeApp.addSubApp(runtimeSubApp);
            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

            Activity runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_SHELL_LOGIN);
            runtimeSubApp.addActivity(runtimeActivity);

            Fragment runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_SHELL_LOGIN.getKey());
            runtimeActivity.addFragment(Fragments.CWP_SHELL_LOGIN.getKey(), runtimeFragment);

            TitleBar runtimeTitleBar;
            SideMenu runtimeSideMenu;
            MainMenu runtimeMainMenu;
            MenuItem runtimeMenuItem;
            TabStrip runtimeTabStrip;
            StatusBar statusBar;

            Tab runtimeTab;


            /**
             * Definition of Developer Manager App
             */

            createDeveloperSubAppNavigationStructure();

            /**
             * End of Developer tabs.
             */

            //wallet factory app
            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.CWP_WALLET_FACTORY);
            String factory_public_key = "public_key_factory";
            runtimeSubApp.setPublicKey(factory_public_key);
            runtimeApp.addSubApp(runtimeSubApp);
            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_WALLET_FACTORY_MAIN);
            runtimeActivity.setColor("#b46a54");
            runtimeActivity.setBackPublicKey(factory_public_key);
            runtimeSubApp.addPosibleStartActivity(Activities.CWP_WALLET_FACTORY_MAIN);
            //runtimeActivity.setStatusBarColor("");


            statusBar = new StatusBar();
            statusBar.setColor("#b46a54");

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Wallet Factory");
            runtimeActivity.setTitleBar(runtimeTitleBar);

            runtimeTabStrip = new TabStrip();
            runtimeTabStrip.setTabsColor("#d07b62");
            runtimeTabStrip.setTabsTextColor("#FFFFFF");
            runtimeTabStrip.setTabsIndicateColor("#b46a54");

            runtimeTab = new Tab();
            runtimeTab.setLabel("Developer Projects");
            runtimeTab.setFragment(Fragments.CWP_WALLET_FACTORY_DEVELOPER_PROJECTS);

            runtimeTabStrip.addTab(runtimeTab);

            runtimeTab = new Tab();
            runtimeTab.setLabel("Edit Mode");
            runtimeTab.setFragment(Fragments.CWP_WALLET_FACTORY_AVAILABLE_PROJECTS);

            runtimeTabStrip.addTab(runtimeTab);


            runtimeActivity.setTabStrip(runtimeTabStrip);
            runtimeSubApp.addActivity(runtimeActivity);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_FACTORY_DEVELOPER_PROJECTS.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_FACTORY_DEVELOPER_PROJECTS.getKey(), runtimeFragment);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_FACTORY_AVAILABLE_PROJECTS.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_FACTORY_AVAILABLE_PROJECTS.getKey(), runtimeFragment);

            /* Adding WizardTypes */
            Wizard runtimeWizard = new Wizard();
            // step 1 wizard create from scratch
            WizardPage runtimeWizardPage = new WizardPage();
            runtimeWizardPage.setType(WizardPageTypes.CWP_WALLET_FACTORY_CREATE_STEP_1);
            runtimeWizard.addPage(runtimeWizardPage);
            //step 2 wizard
            runtimeWizardPage = new WizardPage();
            runtimeWizardPage.setType(WizardPageTypes.CWP_WALLET_FACTORY_CREATE_STEP_2);
            runtimeWizard.addPage(runtimeWizardPage);
            /* Adding wizard */
            runtimeActivity.addWizard(WizardTypes.CWP_WALLET_FACTORY_CREATE_NEW_PROJECT.getKey(), runtimeWizard);

            /**
             *  Edit Activity
             */
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_WALLET_FACTORY_EDIT_WALLET);
            runtimeActivity.setColor("#b46a54");
            runtimeActivity.setBackPublicKey(factory_public_key);
            statusBar = new StatusBar();
            statusBar.setColor("#b46a54");

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Edit Wallet");
            runtimeActivity.setTitleBar(runtimeTitleBar);

            runtimeTabStrip = new TabStrip();

            runtimeTabStrip.setTabsColor("#8bba9e");

            runtimeTabStrip.setTabsTextColor("#FFFFFF");

            runtimeTabStrip.setTabsIndicateColor("#72af9c");

            runtimeTab = new Tab();
            runtimeTab.setLabel("Balance");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_BITCOIN_ALL_BITDUBAI_BALANCE);
            runtimeTabStrip.addTab(runtimeTab);

            runtimeTab = new Tab();
            runtimeTab.setLabel("Contacts");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_BITCOIN_ALL_BITDUBAI_CONTACTS);
            runtimeTabStrip.addTab(runtimeTab);

            runtimeTabStrip.setDividerColor(0x72af9c);
            //runtimeTabStrip.setBackgroundColor("#72af9c");
            runtimeActivity.setTabStrip(runtimeTabStrip);
            runtimeActivity.setBackPublicKey(factory_public_key);
            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_BITCOIN_ALL_BITDUBAI_BALANCE.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_BITCOIN_ALL_BITDUBAI_BALANCE.getKey(), runtimeFragment);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_BITCOIN_ALL_BITDUBAI_CONTACTS.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_BITCOIN_ALL_BITDUBAI_CONTACTS.getKey(), runtimeFragment);


            runtimeSubApp.addActivity(runtimeActivity);


            /**End Wallet Factory*/

            //wallet Publisher app
            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.CWP_WALLET_PUBLISHER);
            runtimeSubApp.setPublicKey("public_key_publisher");
            runtimeApp.addSubApp(runtimeSubApp);
            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_WALLET_PUBLISHER_MAIN);
            runtimeActivity.setColor("#b46a54");
            //runtimeActivity.setStatusBarColor("#b46a54");556
            runtimeSubApp.addPosibleStartActivity(Activities.CWP_WALLET_PUBLISHER_MAIN);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Wallet Publisher");
            runtimeActivity.setTitleBar(runtimeTitleBar);
            runtimeSubApp.addActivity(runtimeActivity);

            runtimeWizard = new Wizard();
            runtimeWizardPage = new WizardPage();
            runtimeWizardPage.setType(WizardPageTypes.CWP_WALLET_PUBLISHER_PUBLISH_STEP_1);
            runtimeWizard.addPage(runtimeWizardPage);

            runtimeWizardPage = new WizardPage();
            runtimeWizardPage.setType(WizardPageTypes.CWP_WALLET_PUBLISHER_PUBLISH_STEP_2);
            runtimeWizard.addPage(runtimeWizardPage);

            runtimeWizardPage = new WizardPage();
            runtimeWizardPage.setType(WizardPageTypes.CWP_WALLET_PUBLISHER_PUBLISH_STEP_3);
            runtimeWizard.addPage(runtimeWizardPage);

            runtimeActivity.addWizard(WizardTypes.CWP_WALLET_PUBLISHER_PUBLISH_PROJECT.getKey(), runtimeWizard);

            runtimeSideMenu = new SideMenu();
            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Personal Wallets");
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Shops");
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Commercial wallets");
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Factory Projects");
            runtimeMenuItem.setLinkToActivity(Activities.CWP_WALLET_FACTORY_MAIN);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Published Wallets");
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Wallet Store");
            runtimeMenuItem.setLinkToActivity(Activities.CWP_WALLET_STORE_MAIN_ACTIVITY);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);


            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Exit");
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeActivity.setSideMenu(runtimeSideMenu);


            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_PUBLISHER_MAIN_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_PUBLISHER_MAIN_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.CWP_WALLET_PUBLISHER_MAIN_FRAGMENT.getKey());


            /**End Wallet Publisher*/

            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.CWP_WALLET_MANAGER);
            runtimeSubApp.setPublicKey("public_key_wallet_manager");
            runtimeApp.addSubApp(runtimeSubApp);
            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

            //TODO: testing
            //lastSubapp = SubApps.CWP_WALLET_MANAGER;

            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_WALLET_MANAGER_MAIN);
            runtimeSubApp.addActivity(runtimeActivity);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_MANAGER_MAIN.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_MANAGER_MAIN.getKey(), runtimeFragment);


            //Desktop page Developer sub App
            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_SUB_APP_DEVELOPER.getKey());
            runtimeActivity.addFragment(Fragments.CWP_SUB_APP_DEVELOPER.getKey(), runtimeFragment);


            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.CWP_WALLET_RUNTIME);
            runtimeApp.addSubApp(runtimeSubApp);
            runtimeSubApp.setPublicKey("public_key_runtime");
            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);


//            runtimeSubApp = new RuntimeSubApp();
//            runtimeSubApp.setType(SubApps.CWP_WALLET_STORE);
//            runtimeApp.addSubApp(runtimeSubApp);
//            listSubApp.put(SubApps.CWP_WALLET_STORE, runtimeSubApp);

            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_WALLET_STORE_MAIN_ACTIVITY);
            runtimeSubApp.addActivity(runtimeActivity);


            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_PUBLISHER_MAIN_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_PUBLISHER_MAIN_FRAGMENT.getKey(), runtimeFragment);


            /**
             * Definition of Shop Manager
             */

            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CWP_WALLET_ADULTS_ALL_SHOPS);
            runtimeActivity.setColor("#76dc4a");
            runtimeSubApp.addActivity(runtimeActivity);


            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("My Shop");

            runtimeActivity.setTitleBar(runtimeTitleBar);


            runtimeTabStrip = new TabStrip();
            runtimeTab = new Tab();
            runtimeTab.setLabel("Shop");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_SHOP);
            runtimeTabStrip.addTab(runtimeTab);
            runtimeTab = new Tab();
            runtimeTab.setLabel("Products");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_PRODUCTS);
            runtimeTabStrip.addTab(runtimeTab);
            runtimeTab = new Tab();
            runtimeTab.setLabel("Reviews");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_REVIEWS);
            runtimeTabStrip.addTab(runtimeTab);
            runtimeTab = new Tab();
            runtimeTab.setLabel("Chat");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_CHAT);
            runtimeTabStrip.addTab(runtimeTab);
            runtimeTab = new Tab();
            runtimeTab.setLabel("History");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_HISTORY);
            runtimeTabStrip.addTab(runtimeTab);
            runtimeTab = new Tab();
            runtimeTab.setLabel("Map");
            runtimeTab.setFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_MAP);
            runtimeTabStrip.addTab(runtimeTab);


            runtimeTabStrip.setDividerColor(0xFFFFFFFF);
            runtimeTabStrip.setIndicatorColor(0xFFFFFFFF);
            runtimeTabStrip.setIndicatorHeight(9);
            runtimeTabStrip.setBackgroundColor(0xFF76dc4a);
            runtimeTabStrip.setTextColor(0xFFFFFFFF);
            runtimeActivity.setTabStrip(runtimeTabStrip);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_SHOP.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_SHOP.getKey(), runtimeFragment);


            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_PRODUCTS.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_PRODUCTS.getKey(), runtimeFragment);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_REVIEWS.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_REVIEWS.getKey(), runtimeFragment);


            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_CHAT.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_CHAT.getKey(), runtimeFragment);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_HISTORY.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_HISTORY.getKey(), runtimeFragment);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_MAP.getKey());
            runtimeActivity.addFragment(Fragments.CWP_WALLET_RUNTIME_WALLET_ADULTS_ALL_BITDUBAI_SHOP_MAP.getKey(), runtimeFragment);
            /**
             * End of SHOPS tabs.
             */


            /**
             * WALLET STORE
             */


            createWalletStoreNavigationStructure();



            /**
             * CCP Intra User Community SubApp
             */
            RuntimeSubApp subAppIntraUser = new RuntimeSubApp();
            subAppIntraUser.setType(SubApps.CCP_INTRA_USER_COMMUNITY);
            String communityPublicKey = "public_key_intra_user_commmunity";
            subAppIntraUser.setPublicKey(communityPublicKey);
            listSubApp.put(subAppIntraUser.getPublicKey(), subAppIntraUser);

            //Activity Explore
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeActivity.setActivityType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD.getCode());
            subAppIntraUser.addPosibleStartActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeActivity.setColor("#FF0B46F0");

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabelSize(20);
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#0072bb");

            runtimeActivity.setTitleBar(runtimeTitleBar);

/*            runtimeMainMenu = new MainMenu();
            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Settings");
            runtimeMainMenu.addMenuItem(runtimeMenuItem);

            runtimeActivity.setMainMenu(runtimeMainMenu);*/

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD_FRAGMENT.getKey());


            runtimeSideMenu = new SideMenu();
            runtimeSideMenu.setBackgroundColor("#0072bb");

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Cripto wallet users");
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Connections");
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST);
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Notifications");
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS);
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeActivity.setSideMenu(runtimeSideMenu);

            subAppIntraUser.addActivity(runtimeActivity);

            // Activity: Connection
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST);
            runtimeActivity.setActivityType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST.getCode());
            runtimeActivity.setBackActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeActivity.setBackPublicKey(communityPublicKey);
            runtimeActivity.setColor("#FF0B46F0");

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Connections");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#0072bb");
            runtimeTitleBar.setLabelSize(20);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST_FRAGMENT.getKey());

            runtimeSideMenu = new SideMenu();
            runtimeSideMenu.setBackgroundColor("#0072bb");

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Cripto wallet users");
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Connections");
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST);
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Notifications");
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS);
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeActivity.setSideMenu(runtimeSideMenu);
            subAppIntraUser.addActivity(runtimeActivity);

            // Activity: Notifications
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS);
            runtimeActivity.setActivityType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS.getCode());
            runtimeActivity.setBackActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeActivity.setBackPublicKey(communityPublicKey);
            runtimeActivity.setColor("#FF0B46F0");

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Notifications");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#0072bb");
            runtimeTitleBar.setLabelSize(20);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS_FRAGMENT.getKey());

            runtimeSideMenu = new SideMenu();
            runtimeSideMenu.setBackgroundColor("#0072bb");
            runtimeSideMenu.setHasFooter(false);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Cripto wallet users");
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Connections");
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_FRIEND_LIST);
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeMenuItem = new MenuItem();
            runtimeMenuItem.setLabel("Notifications");
            runtimeMenuItem.setLinkToActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_NOTIFICATIONS);
            runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
            runtimeSideMenu.addMenuItem(runtimeMenuItem);

            runtimeActivity.setSideMenu(runtimeSideMenu);

            subAppIntraUser.addActivity(runtimeActivity);

            // Activity: Other Profile
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_OTHER_PROFILE);
            runtimeActivity.setActivityType(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_OTHER_PROFILE.getCode());
            runtimeActivity.setBackActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_WORLD);
            runtimeActivity.setBackPublicKey(communityPublicKey);
            runtimeActivity.setColor("#FF0B46F0");

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#0072bb");
            runtimeTitleBar.setIconName("back");
            runtimeTitleBar.setLabelSize(20);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#0072bb");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey());

            subAppIntraUser.addActivity(runtimeActivity);
            /**
             * End of community intra user CCP
             */

            /**
             * Start DAP
             */

            /**
             * DAP ASSET FACTORY
             */
            createAssetFactorySubAppNavigationStructure();
            /**
             * DAP ASSET ISSUER COMMUNITY
             */
            createAssetIssuerCommunitySubAppNavigationStructure();

            /**
             * DAP ASSET USER COMMUNITY
             */
            createAssetUserCommunitySubAppNavigationStructure();

            /**
             * DAP REDEEM POINT COMMUNITY
             */
            createRedeemPointCommunitySubAppNavigationStructure();

            /**
             * End DAP
             */

            /**
             * CRYPTO BROKER IDENTITY
             */
            createCryptoBrokerIdentitySubAppNavigationStructure();

            /**
             * CBP CRYPTO CUSTOMER  IDENTITY
             */
            createCryptoCustomerIdentitySubAppNavigationStructure();

            /**
             * CBP CRYPTO BROKER COMMUNITY
             */
            createCryptoBrokerCommunitySubAppNavigationStructure();

            /**
             * CBP CRYPTO CUSTOMER COMMUNITY
             */
            createCryptoCustomerCommunitySubAppNavigationStructure();

            /**
             * CHT CHAT
             */
            createChatSubAppNavigationStructure();

            /**
             * CCP INTRA USER IDENTITY
             */
            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.CWP_INTRA_USER_IDENTITY);
            String intraUserIdentityPublicKey = "public_key_ccp_intra_user_identity";
            runtimeSubApp.setPublicKey(intraUserIdentityPublicKey);

            // Activity: List of identities
//            runtimeActivity = new Activity();
//            runtimeActivity.setType(Activities.CCP_SUB_APP_INTRA_USER_IDENTITY);
//            runtimeActivity.setActivityType(Activities.CCP_SUB_APP_INTRA_USER_IDENTITY.getCode());
//            runtimeActivity.setBackActivity(null);
//            runtimeActivity.setColor("#03A9F4");
//            runtimeSubApp.addActivity(runtimeActivity);
//
//
//            runtimeTitleBar = new TitleBar();
//            runtimeTitleBar.setLabel("Intra user Identity");
//            runtimeTitleBar.setIsTitleTextStatic(true);
//            runtimeTitleBar.setColor("#1189a4");
//            runtimeTitleBar.setTitleColor("#ffffff");
//            runtimeTitleBar.setLabelSize(18);
//            runtimeActivity.setTitleBar(runtimeTitleBar);
//
//            statusBar = new StatusBar();
//            statusBar.setColor("#1189a4");
//            runtimeActivity.setStatusBar(statusBar);
//
//            runtimeFragment = new Fragment();
//            runtimeFragment.setType(Fragments.CCP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_MAIN_FRAGMENT.getKey());
//            runtimeActivity.addFragment(Fragments.CCP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_MAIN_FRAGMENT.getKey(), runtimeFragment);
//            runtimeActivity.setStartFragment(Fragments.CCP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_MAIN_FRAGMENT.getKey());

            // Activity: Create New Identity
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.CCP_SUB_APP_INTRA_IDENTITY_CREATE_IDENTITY);
            runtimeActivity.setActivityType(Activities.CCP_SUB_APP_INTRA_IDENTITY_CREATE_IDENTITY.getCode());
            //runtimeActivity.setBackActivity(Activities.CCP_SUB_APP_INTRA_USER_IDENTITY);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
            runtimeSubApp.addPosibleStartActivity(Activities.CCP_SUB_APP_INTRA_IDENTITY_CREATE_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Identity Manager");
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.CCP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.CCP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.CCP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());

            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

            /**
             * DAP IDENTITIES
             */

            /**
             * DAP ISSUER IDENTITY
             */
            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.DAP_ASSETS_IDENTITY_ISSUER);
            runtimeSubApp.setPublicKey("public_key_dap_asset_issuer_identity");

            // Activity: List of identities
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY);
            runtimeActivity.setActivityType(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY.getCode());
            runtimeActivity.setBackActivity(null);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
//            runtimeSubApp.addPosibleStartActivity(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Issuer Identity");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_MAIN_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_MAIN_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_MAIN_FRAGMENT.getKey());

            // Activity: Create New Identity
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_CREATE_IDENTITY);
            runtimeActivity.setActivityType(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_CREATE_IDENTITY.getCode());
//            runtimeActivity.setBackActivity(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
            runtimeSubApp.addPosibleStartActivity(Activities.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_CREATE_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Create New Issuer Identity");
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_ISSUER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());

            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);


            /**
             * DAP USER IDENTITY
             */
            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.DAP_ASSETS_IDENTITY_USER);
            runtimeSubApp.setPublicKey("public_key_dap_asset_user_identity");

            // Activity: List of identities
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY);
            runtimeActivity.setActivityType(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY.getCode());
            runtimeActivity.setBackActivity(null);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
//            runtimeSubApp.addPosibleStartActivity(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("User Identity");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_USER_IDENTITY_MAIN_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_USER_IDENTITY_MAIN_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_USER_IDENTITY_MAIN_FRAGMENT.getKey());

            // Activity: Create New Identity
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY);
            runtimeActivity.setActivityType(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY.getCode());
//            runtimeActivity.setBackActivity(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
            runtimeSubApp.addPosibleStartActivity(Activities.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Create New User Identity");
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_USER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());

            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

            /**
             * REDEEM POINT IDENTITY
             */
            runtimeSubApp = new RuntimeSubApp();
            runtimeSubApp.setType(SubApps.DAP_REDEEM_POINT_IDENTITY);
            runtimeSubApp.setPublicKey("public_key_dap_redeem_point_identity");

            // Activity: List of identities
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY);
            runtimeActivity.setActivityType(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY.getCode());
            runtimeActivity.setBackActivity(null);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
//            runtimeSubApp.addPosibleStartActivity(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Redeem Point Identity");
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.DAP_SUB_APP_REDEEM_POINT_IDENTITY_MAIN_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.DAP_SUB_APP_REDEEM_POINT_IDENTITY_MAIN_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_REDEEM_POINT_IDENTITY_MAIN_FRAGMENT.getKey());

            // Activity: Create New Identity
            runtimeActivity = new Activity();
            runtimeActivity.setType(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY);
            runtimeActivity.setActivityType(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY.getCode());
//            runtimeActivity.setBackActivity(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY);
            runtimeActivity.setColor("#03A9F4");
            runtimeSubApp.addActivity(runtimeActivity);
            runtimeSubApp.addPosibleStartActivity(Activities.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY);

            runtimeTitleBar = new TitleBar();
            runtimeTitleBar.setLabel("Create New Redeem Point Identity");
            runtimeTitleBar.setColor("#1189a4");
            runtimeTitleBar.setTitleColor("#ffffff");
            runtimeTitleBar.setLabelSize(18);
            runtimeTitleBar.setIsTitleTextStatic(true);
            runtimeActivity.setTitleBar(runtimeTitleBar);

            statusBar = new StatusBar();
            statusBar.setColor("#1189a4");
            runtimeActivity.setStatusBar(statusBar);

            runtimeFragment = new Fragment();
            runtimeFragment.setType(Fragments.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());
            runtimeActivity.addFragment(Fragments.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
            runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_REDEEM_POINT_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());

            listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);


        } catch (Exception e) {
            String message = com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.exceptions.CantFactoryResetException.DEFAULT_MESSAGE;
            FermatException cause = FermatException.wrapException(e);
            String context = "Error on method Factory Reset, setting the structure of the apps";
            String possibleReason = "some null definition";
            throw new com.bitdubai.fermat_dmp_plugin.layer.engine.sub_app_runtime.developer.bitdubai.version_1.exceptions.CantFactoryResetException(message, cause, context, possibleReason);

        }

    }

    private void createWalletStoreNavigationStructure() {
        RuntimeSubApp runtimeSubApp;
        Activity runtimeActivity;
        StatusBar statusBar;
        TitleBar runtimeTitleBar;
        Fragment runtimeFragment;

        String publicKey = "public_key_store";

        runtimeSubApp = new RuntimeSubApp();
        runtimeSubApp.setType(SubApps.CWP_WALLET_STORE);
        runtimeSubApp.setPublicKey(publicKey);
        listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

        //List of wallets
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_WALLET_STORE_MAIN_ACTIVITY);
        runtimeActivity.setActivityType(Activities.CWP_WALLET_STORE_MAIN_ACTIVITY.getCode());
        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CWP_WALLET_STORE_MAIN_ACTIVITY);

        statusBar = new StatusBar();
        statusBar.setColor("#B4573C");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Wallet Store");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#B46A54");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_STORE_MAIN_ACTIVITY.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_STORE_MAIN_ACTIVITY.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_STORE_MAIN_ACTIVITY.getKey());

        //Wallet Details
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_WALLET_STORE_DETAIL_ACTIVITY);
        runtimeActivity.setBackActivity(Activities.CWP_WALLET_STORE_MAIN_ACTIVITY);
        runtimeActivity.setBackPublicKey(publicKey);
        runtimeSubApp.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#B4573C");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Wallet Details");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#B46A54");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_STORE_DETAIL_ACTIVITY.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_STORE_DETAIL_ACTIVITY.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_STORE_DETAIL_ACTIVITY.getKey());

        //More Details
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_WALLET_STORE_MORE_DETAIL_ACTIVITY);
        runtimeActivity.setColor("#FFFFFF");
        runtimeActivity.setBackActivity(Activities.CWP_WALLET_STORE_DETAIL_ACTIVITY);
        runtimeActivity.setBackPublicKey(publicKey);
        runtimeSubApp.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#AAAAAA");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("More Details");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#222222");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#FFFFFF");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_STORE_MORE_DETAIL_ACTIVITY.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_STORE_MORE_DETAIL_ACTIVITY.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_STORE_MORE_DETAIL_ACTIVITY.getKey());
    }

    private void createAssetFactorySubAppNavigationStructure() {
        RuntimeSubApp dapFactory;
        Activity runtimeActivity;
        Tab runtimeTab;
        TabStrip runtimeTabStrip;
        TitleBar runtimeTitleBar;
        StatusBar statusBar;
        Fragment runtimeFragment;
        SideMenu runtimeSideMenu;
        MenuItem runtimeMenuItem;

        String dapFactoryPublicKey = "public_key_dap_factory";

        dapFactory = new RuntimeSubApp();
        dapFactory.setType(SubApps.DAP_ASSETS_FACTORY);
        dapFactory.addPosibleStartActivity(Activities.DAP_MAIN);
        dapFactory.setPublicKey(dapFactoryPublicKey);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_MAIN);
        runtimeActivity.setColor("#1d1d25");
        runtimeActivity.setBackPublicKey(dapFactoryPublicKey);

        statusBar = new StatusBar();
        statusBar.setColor("#1d1d25");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Asset Factory");
        runtimeTitleBar.setColor("#1d1d25");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeTabStrip = new TabStrip();
        runtimeTabStrip.setTabsColor("#1d1d25");
        runtimeTabStrip.setTabsIndicateColor("#ffffff");
        runtimeTabStrip.setTabsTextColor("#ffffff");

        runtimeTab = new Tab();
        runtimeTab.setLabel("Draft");
        runtimeTab.setFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_EDITABLE_TAB_FRAGMENT);
        runtimeTabStrip.addTab(runtimeTab);

        runtimeTab = new Tab();
        runtimeTab.setLabel("Issued");
        runtimeTab.setFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_PUBLISHED_TAB_FRAGMENT);
        runtimeTabStrip.addTab(runtimeTab);

        runtimeActivity.setTabStrip(runtimeTabStrip);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_FACTORY_EDITABLE_TAB_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_EDITABLE_TAB_FRAGMENT.getKey(), runtimeFragment);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_FACTORY_PUBLISHED_TAB_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_PUBLISHED_TAB_FRAGMENT.getKey(), runtimeFragment);

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#1d1d25");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(dapFactoryPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Settings");
        runtimeMenuItem.setAppLinkPublicKey(dapFactoryPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapFactory.addActivity(runtimeActivity);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_EDITOR_ACTIVITY);
        runtimeActivity.setBackActivity(Activities.DAP_MAIN);
        runtimeActivity.setBackPublicKey(dapFactoryPublicKey);
        runtimeActivity.setColor("#1d1d25");

        statusBar = new StatusBar();
        statusBar.setColor("#1d1d25");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Draft Asset");
        runtimeTitleBar.setColor("#1d1d25");

        runtimeActivity.setTitleBar(runtimeTitleBar);
        runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_EDITOR_ACTIVITY.getKey());

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_EDITOR_ACTIVITY.getKey());
        runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_EDITOR_ACTIVITY.getKey(), runtimeFragment);

        dapFactory.addActivity(runtimeActivity);

        //DAP FACTORY SETTINGS
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS);
        runtimeActivity.setBackActivity(Activities.DAP_MAIN);
        runtimeActivity.setBackPublicKey(dapFactoryPublicKey);
        runtimeActivity.setColor("#1d1d25");

        statusBar = new StatusBar();
        statusBar.setColor("#1d1d25");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Settings");
        runtimeTitleBar.setColor("#1d1d25");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS.getKey());

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS.getKey());
        runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS.getKey(), runtimeFragment);

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#1d1d25");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(dapFactoryPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Settings");
        runtimeMenuItem.setAppLinkPublicKey(dapFactoryPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapFactory.addActivity(runtimeActivity);

        //DAP FACTORY SETTINGS_NETWORK
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NETWORK_MAIN);
        runtimeActivity.setBackActivity(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS);
        runtimeActivity.setBackPublicKey(dapFactoryPublicKey);
        runtimeActivity.setColor("#1d1d25");

        statusBar = new StatusBar();
        statusBar.setColor("#1d1d25");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Network");
        runtimeTitleBar.setColor("#1d1d25");
        runtimeTitleBar.setIconName("Back");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NETWORK_MAIN.getKey());

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NETWORK_MAIN.getKey());
        runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NETWORK_MAIN.getKey(), runtimeFragment);

        dapFactory.addActivity(runtimeActivity);

        //DAP FACTORY SETTINGS_NOTIFICATIONS
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NOTIFICATIONS);
        runtimeActivity.setBackActivity(Activities.DAP_SUB_APP_ASSET_FACTORY_SETTINGS);
        runtimeActivity.setBackPublicKey(dapFactoryPublicKey);
        runtimeActivity.setColor("#1d1d25");

        statusBar = new StatusBar();
        statusBar.setColor("#1d1d25");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Notifications");
        runtimeTitleBar.setColor("#1d1d25");
        runtimeTitleBar.setIconName("Back");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeActivity.setStartFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NOTIFICATIONS.getKey());

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NOTIFICATIONS.getKey());
        runtimeActivity.addFragment(Fragments.DAP_SUB_APP_ASSET_FACTORY_SETTINGS_NOTIFICATIONS.getKey(), runtimeFragment);

        dapFactory.addActivity(runtimeActivity);

        listSubApp.put(dapFactory.getPublicKey(), dapFactory);
    }

    private void createAssetIssuerCommunitySubAppNavigationStructure() {
        RuntimeSubApp dapAssetIssuerCommunity;
        Activity runtimeActivity;
        TitleBar runtimeTitleBar;
        StatusBar statusBar;
        Fragment runtimeFragment;
        SideMenu runtimeSideMenu;
        MenuItem runtimeMenuItem;

        String communityIssuerPublicKey = "public_key_dap_issuer_community";

        dapAssetIssuerCommunity = new RuntimeSubApp();
        dapAssetIssuerCommunity.setType(SubApps.DAP_ASSETS_COMMUNITY_ISSUER);
        dapAssetIssuerCommunity.setPublicKey(communityIssuerPublicKey);
        dapAssetIssuerCommunity.addPosibleStartActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN.getCode());
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Asset Issuer Community");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityIssuerPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityIssuerPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityIssuerPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetIssuerCommunity.addActivity(runtimeActivity);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_CONNECTIONS_LIST);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_CONNECTIONS_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityIssuerPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Connections");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_ISSUER_COMMUNITY_CONNECTIONS_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_ISSUER_COMMUNITY_CONNECTIONS_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_ISSUER_COMMUNITY_CONNECTIONS_LIST_FRAGMENT.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityIssuerPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityIssuerPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityIssuerPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetIssuerCommunity.addActivity(runtimeActivity);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_PROFILE);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_PROFILE.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityIssuerPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new com.bitdubai.fermat_api.layer.all_definition.navigation_structure.StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Profile");
        runtimeTitleBar.setIconName("Back");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_PROFILE_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_PROFILE_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_ISSUER_COMMUNITY_ACTIVITY_PROFILE_FRAGMENT.getKey());

        dapAssetIssuerCommunity.addActivity(runtimeActivity);

        listSubApp.put(dapAssetIssuerCommunity.getPublicKey(), dapAssetIssuerCommunity);
    }

    private void createAssetUserCommunitySubAppNavigationStructure() {
        RuntimeSubApp dapAssetUserCommunity;
        Activity runtimeActivity;
        TitleBar runtimeTitleBar;
        StatusBar statusBar;
        Fragment runtimeFragment;
        SideMenu runtimeSideMenu;
        MenuItem runtimeMenuItem;

        String communityUserPublicKey = "public_key_dap_user_community";

        dapAssetUserCommunity = new RuntimeSubApp();
        dapAssetUserCommunity.setType(SubApps.DAP_ASSETS_COMMUNITY_USER);
        dapAssetUserCommunity.setPublicKey(communityUserPublicKey);
        dapAssetUserCommunity.addPosibleStartActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN.getCode());
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Asset User Community");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN.getKey());
        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN.getKey(), runtimeFragment);

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Manage Groups");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);
        dapAssetUserCommunity.addActivity(runtimeActivity);

        //INI User other profile activity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_SUB_APP_ASSET_USER_COMMUNITY_CONNECTION_OTHER_PROFILE);
        runtimeActivity.setActivityType(Activities.DAP_SUB_APP_ASSET_USER_COMMUNITY_CONNECTION_OTHER_PROFILE.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityUserPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Profile");
        runtimeTitleBar.setIconName("Back");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_USER_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_USER_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_USER_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey());

        dapAssetUserCommunity.addActivity(runtimeActivity);


        //INI User connections list activity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityUserPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Connections");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_USER_COMMUNITY_CONNECTION_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_USER_COMMUNITY_CONNECTION_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_USER_COMMUNITY_CONNECTION_LIST_FRAGMENT.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Manage Groups");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetUserCommunity.addActivity(runtimeActivity);

        // Activity: Manage Groups

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityUserPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Manage Groups");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_FRAGMENT.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Manage Groups");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetUserCommunity.addActivity(runtimeActivity);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_USERS_FRAGMENT);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_USERS_FRAGMENT.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeActivity.setBackPublicKey(communityUserPublicKey);
        runtimeActivity.setColor("#FF0B46F0");



        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        //runtimeTitleBar.setLabel("GROUP NAME");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_USERS_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_USERS_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_USERS_FRAGMENT.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Manage Groups");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetUserCommunity.addActivity(runtimeActivity);


        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_USERS);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_USERS.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_USERS_FRAGMENT);
        runtimeActivity.setBackPublicKey(communityUserPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        //runtimeTitleBar.setLabel("GROUP NAME");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_USERS.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_USERS.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_USERS.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Manage Groups");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_USER_COMMUNITY_ACTIVITY_ADMINISTRATIVE_GROUP_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityUserPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetUserCommunity.addActivity(runtimeActivity);

        listSubApp.put(dapAssetUserCommunity.getPublicKey(), dapAssetUserCommunity);
    }

    private void createRedeemPointCommunitySubAppNavigationStructure() {
        RuntimeSubApp dapAssetRedeemPointCommunity;
        Activity runtimeActivity;
        TitleBar runtimeTitleBar;
        StatusBar statusBar;
        Fragment runtimeFragment;
        SideMenu runtimeSideMenu;
        MenuItem runtimeMenuItem;

        String communityRedeemPointPublicKey = "public_key_dap_redeem_point_community";

        dapAssetRedeemPointCommunity = new RuntimeSubApp();
        dapAssetRedeemPointCommunity.setType(SubApps.DAP_ASSETS_COMMUNITY_REDEEM_POINT);
        dapAssetRedeemPointCommunity.setPublicKey(communityRedeemPointPublicKey);
        dapAssetRedeemPointCommunity.addPosibleStartActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN.getCode());
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Redeem Point Community");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN.getKey());
        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN.getKey(), runtimeFragment);

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityRedeemPointPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityRedeemPointPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityRedeemPointPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetRedeemPointCommunity.addActivity(runtimeActivity);


        //INI redeem point community other profile

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_OTHER_PROFILE);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_OTHER_PROFILE.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityRedeemPointPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Profile");
        runtimeTitleBar.setIconName("Back");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_OTHER_PROFILE_FRAGMENT.getKey());

        dapAssetRedeemPointCommunity.addActivity(runtimeActivity);

        //INI Redeem Point connections list activity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTIONS_LIST);
        runtimeActivity.setActivityType(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTIONS_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeActivity.setBackPublicKey(communityRedeemPointPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Connections");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTION_LIST_FRAGMENT.getKey());

        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Home");
        runtimeMenuItem.setAppLinkPublicKey(communityRedeemPointPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_CONNECTIONS_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityRedeemPointPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.DAP_ASSET_REDEEM_POINT_COMMUNITY_ACTIVITY_MAIN);
        runtimeMenuItem.setAppLinkPublicKey(communityRedeemPointPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeActivity.setSideMenu(runtimeSideMenu);

        dapAssetRedeemPointCommunity.addActivity(runtimeActivity);


        listSubApp.put(dapAssetRedeemPointCommunity.getPublicKey(), dapAssetRedeemPointCommunity);
    }

    private void createCryptoCustomerIdentitySubAppNavigationStructure() {
        RuntimeSubApp runtimeSubApp;
        Activity runtimeActivity;
        TitleBar runtimeTitleBar;
        StatusBar statusBar;
        Fragment runtimeFragment;

        String publicKey = "sub_app_crypto_customer_identity";

        runtimeSubApp = new RuntimeSubApp();
        runtimeSubApp.setType(SubApps.CBP_CRYPTO_CUSTOMER_IDENTITY);
        runtimeSubApp.setPublicKey(publicKey);
        listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

        // Activity: List of identities
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY.getCode());
        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY);

        statusBar = new StatusBar();
        statusBar.setColor("#0288D1");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Crypto Customer Identity");
        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setTitleColor("#03A9F4");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_MAIN_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_MAIN_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_MAIN_FRAGMENT.getKey());

        // Activity: Create New Identity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY);
        runtimeActivity.setBackPublicKey(publicKey);
        runtimeSubApp.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0288D1");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Create New Identity");
        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setTitleColor("#03A9F4");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());

        // Activity: Edit Identity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_EDIT_IDENTITY);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_EDIT_IDENTITY.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY);
        runtimeActivity.setBackPublicKey(publicKey);
        runtimeSubApp.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0288D1");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Edit Identity");
        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setTitleColor("#03A9F4");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_EDIT_IDENTITY_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_EDIT_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_IDENTITY_EDIT_IDENTITY_FRAGMENT.getKey());
    }

    private void createCryptoBrokerIdentitySubAppNavigationStructure() {
        RuntimeSubApp runtimeSubApp;
        Activity runtimeActivity;
        StatusBar statusBar;
        TitleBar runtimeTitleBar;
        Fragment runtimeFragment;

        String publicKey = "sub_app_crypto_broker_identity";

        runtimeSubApp = new RuntimeSubApp();
        runtimeSubApp.setType(SubApps.CBP_CRYPTO_BROKER_IDENTITY);
        runtimeSubApp.setPublicKey(publicKey);
        listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);

        // Activity: List of identities
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY.getCode());
        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Broker Identity");
        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        //runtimeTitleBar.setIconName("Back");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_MAIN_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_MAIN_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_MAIN_FRAGMENT.getKey());

        // Activity: Create New Identity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_CREATE_IDENTITY);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_CREATE_IDENTITY.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY);
        runtimeActivity.setBackPublicKey(publicKey);
        runtimeSubApp.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("New Identity");
        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeTitleBar.setIconName("Back");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_CREATE_IDENTITY_FRAGMENT.getKey());

        // Activity: Edit Identity
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_EDIT_IDENTITY);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_EDIT_IDENTITY.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY);
        runtimeActivity.setBackPublicKey(publicKey);
        runtimeSubApp.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Edit Identity");
        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeTitleBar.setIconName("Back");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_EDIT_IDENTITY_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_EDIT_IDENTITY_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_IDENTITY_EDIT_IDENTITY_FRAGMENT.getKey());
    }

    private void createCryptoBrokerCommunitySubAppNavigationStructure() {
        TitleBar runtimeTitleBar;
        SideMenu runtimeSideMenu;
        MenuItem runtimeMenuItem;
        StatusBar statusBar;
        Activity runtimeActivity;
        Fragment runtimeFragment;

        RuntimeSubApp subAppBrokerCommunity = new RuntimeSubApp();
        subAppBrokerCommunity.setType(SubApps.CBP_CRYPTO_BROKER_COMMUNITY);
        String communityPublicKey = "public_key_crypto_broker_community";
        subAppBrokerCommunity.setPublicKey(communityPublicKey);

        //Side Menu definition
        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Crypto Broker Users");
        runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_FRIEND_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_NOTIFICATIONS);
        runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);



        //Activity: CONNECTION_WORLD
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD.getCode());
        subAppBrokerCommunity.addPosibleStartActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Crypto Broker Users");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD.getKey());

        runtimeActivity.setSideMenu(runtimeSideMenu);
        subAppBrokerCommunity.addActivity(runtimeActivity);


        // Activity: CONNECTION_FRIEND_LIST
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_FRIEND_LIST);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_FRIEND_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setBackPublicKey(communityPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Connections");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_FRIEND_LIST.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_FRIEND_LIST.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_FRIEND_LIST.getKey());

        runtimeActivity.setSideMenu(runtimeSideMenu);
        subAppBrokerCommunity.addActivity(runtimeActivity);


        // Activity: CONNECTION_NOTIFICATIONS
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_NOTIFICATIONS);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_NOTIFICATIONS.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setBackPublicKey(communityPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Notifications");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_NOTIFICATIONS.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_NOTIFICATIONS.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_NOTIFICATIONS.getKey());

        runtimeActivity.setSideMenu(runtimeSideMenu);
        subAppBrokerCommunity.addActivity(runtimeActivity);


        // Activity: CONNECTION_OTHER_PROFILE
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_OTHER_PROFILE);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_OTHER_PROFILE.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setBackPublicKey(communityPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Profile");
        runtimeTitleBar.setIconName("Back");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_OTHER_PROFILE.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_OTHER_PROFILE.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_BROKER_COMMUNITY_CONNECTION_OTHER_PROFILE.getKey());

        subAppBrokerCommunity.addActivity(runtimeActivity);


        listSubApp.put(subAppBrokerCommunity.getPublicKey(), subAppBrokerCommunity);
    }

    private void createCryptoCustomerCommunitySubAppNavigationStructure() {
        TitleBar runtimeTitleBar;
        SideMenu runtimeSideMenu;
        MenuItem runtimeMenuItem;
        StatusBar statusBar;
        Activity runtimeActivity;
        Fragment runtimeFragment;

        RuntimeSubApp subAppCustomerCommunity = new RuntimeSubApp();
        subAppCustomerCommunity.setType(SubApps.CBP_CRYPTO_CUSTOMER_COMMUNITY);
        String communityPublicKey = "public_key_crypto_customer_community";
        subAppCustomerCommunity.setPublicKey(communityPublicKey);

        //Side Menu definition
        runtimeSideMenu = new SideMenu();
        runtimeSideMenu.setBackgroundColor("#0072bb");

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Crypto Customer Users");
        runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
        runtimeMenuItem.setLinkToActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Connections");
        runtimeMenuItem.setLinkToActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_FRIEND_LIST);
        runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);

        runtimeMenuItem = new MenuItem();
        runtimeMenuItem.setLabel("Notifications");
        runtimeMenuItem.setLinkToActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_NOTIFICATIONS);
        runtimeMenuItem.setAppLinkPublicKey(communityPublicKey);
        runtimeSideMenu.addMenuItem(runtimeMenuItem);



        //Activity: CONNECTION_WORLD
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD.getCode());
        subAppCustomerCommunity.addPosibleStartActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Crypto Customer Users");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD.getKey());

        runtimeActivity.setSideMenu(runtimeSideMenu);
        subAppCustomerCommunity.addActivity(runtimeActivity);


        // Activity: CONNECTION_FRIEND_LIST
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_FRIEND_LIST);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_FRIEND_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setBackPublicKey(communityPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Connections");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_FRIEND_LIST.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_FRIEND_LIST.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_FRIEND_LIST.getKey());

        runtimeActivity.setSideMenu(runtimeSideMenu);
        subAppCustomerCommunity.addActivity(runtimeActivity);


        // Activity: CONNECTION_NOTIFICATIONS
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_NOTIFICATIONS);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_NOTIFICATIONS.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setBackPublicKey(communityPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Notifications");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_NOTIFICATIONS.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_NOTIFICATIONS.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_NOTIFICATIONS.getKey());

        runtimeActivity.setSideMenu(runtimeSideMenu);
        subAppCustomerCommunity.addActivity(runtimeActivity);


        // Activity: CONNECTION_OTHER_PROFILE
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_OTHER_PROFILE);
        runtimeActivity.setActivityType(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_OTHER_PROFILE.getCode());
        runtimeActivity.setBackActivity(Activities.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_WORLD);
        runtimeActivity.setBackPublicKey(communityPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Profile");
        runtimeTitleBar.setIconName("Back");
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeTitleBar.setLabelSize(20);
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_OTHER_PROFILE.getKey());
        runtimeActivity.addFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_OTHER_PROFILE.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CBP_SUB_APP_CRYPTO_CUSTOMER_COMMUNITY_CONNECTION_OTHER_PROFILE.getKey());

        subAppCustomerCommunity.addActivity(runtimeActivity);


        listSubApp.put(subAppCustomerCommunity.getPublicKey(), subAppCustomerCommunity);
    }

    private void createDeveloperSubAppNavigationStructure() {
        RuntimeSubApp runtimeSubApp;
        Activity runtimeActivity;
        StatusBar statusBar;
        TitleBar runtimeTitleBar;
        TabStrip runtimeTabStrip;
        Tab runtimeTab;
        Fragment runtimeFragment;
        final int titleBarLabelSize = 20;

        runtimeSubApp = new RuntimeSubApp();
        runtimeSubApp.setType(SubApps.CWP_DEVELOPER_APP);
        runtimeSubApp.setPublicKey("public_key_pip_developer_sub_app");

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_SUB_APP_ALL_DEVELOPER);
        runtimeActivity.setActivityType(Activities.CWP_SUB_APP_ALL_DEVELOPER.getCode());
        runtimeActivity.setColor("#b46a54");

        statusBar = new StatusBar();
        statusBar.setColor("#d07b62");
        runtimeActivity.setStatusBar(statusBar);

        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CWP_SUB_APP_ALL_DEVELOPER);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Developer");
        runtimeTitleBar.setLabelSize(titleBarLabelSize);
        runtimeTitleBar.setTitleColor("#ffffff");
//        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#d07b62");
        runtimeActivity.setTitleBar(runtimeTitleBar);

//        runtimeTabStrip = new TabStrip();
//        runtimeTabStrip.setTabsColor("#d07b62");
//        runtimeTabStrip.setTabsTextColor("#FFFFFF");
//        runtimeTabStrip.setTabsIndicateColor("#b46a54");
//        runtimeActivity.setTabStrip(runtimeTabStrip);
//
//        runtimeTab = new Tab();
//        runtimeTab.setLabel("DataBase Tools");
//        runtimeTab.setFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT);
//        runtimeFragment = new Fragment();
//        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT.getKey());
//        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT.getKey(), runtimeFragment);
//        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT.getKey());
//        runtimeTabStrip.addTab(runtimeTab);
//
//        runtimeTab = new Tab();
//        runtimeTab.setLabel("Log Tools");
//        runtimeTab.setFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_FRAGMENT);
//        runtimeFragment = new Fragment();
//        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_FRAGMENT.getKey());
//        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_FRAGMENT.getKey(), runtimeFragment);
//        runtimeTabStrip.addTab(runtimeTab);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_FRAGMENT.getKey());

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE);
        runtimeActivity.setActivityType(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE.getCode());
        runtimeActivity.setBackActivity(Activities.CWP_SUB_APP_ALL_DEVELOPER);
        runtimeActivity.setBackPublicKey(runtimeSubApp.getPublicKey());
        runtimeActivity.setColor("#b46a54");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Developer > Databases");
        runtimeTitleBar.setLabelSize(titleBarLabelSize);
        runtimeTitleBar.setTitleColor("#ffffff");
//        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#d07b62");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_LIST_FRAGMENT.getKey());
//        runtimeFragment.setBack(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_LIST_FRAGMENT.getKey());

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST);
        runtimeActivity.setActivityType(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE);
        runtimeActivity.setBackPublicKey(runtimeSubApp.getPublicKey());
        runtimeActivity.setColor("#b46a54");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Developer > Tables");
        runtimeTitleBar.setLabelSize(titleBarLabelSize);
        runtimeTitleBar.setTitleColor("#ffffff");
//        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#d07b62");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#d07b62");
        runtimeActivity.setStatusBar(statusBar);

        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST_FRAGMENT.getKey());

        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_RECORD_LIST);
        runtimeActivity.setActivityType(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_RECORD_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_LIST);
        runtimeActivity.setBackPublicKey(runtimeSubApp.getPublicKey());
        runtimeActivity.setColor("#b46a54");

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Developer > Records");
        runtimeTitleBar.setLabelSize(titleBarLabelSize);
        runtimeTitleBar.setTitleColor("#ffffff");
//        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#d07b62");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        statusBar = new StatusBar();
        statusBar.setColor("#d07b62");
        runtimeActivity.setStatusBar(statusBar);

        runtimeSubApp.addActivity(runtimeActivity);
        runtimeSubApp.addPosibleStartActivity(Activities.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_RECORD_LIST);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_RECORD_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_RECORD_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_DATABASE_TABLE_RECORD_LIST_FRAGMENT.getKey());
//
//        runtimeFragment = new Fragment();
//        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_1_FRAGMENT.getKey());
//        runtimeFragment.setBack(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_FRAGMENT.getKey());
//        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_1_FRAGMENT.getKey(), runtimeFragment);
//
//        runtimeFragment = new Fragment();
//        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_2_FRAGMENT.getKey());
//        runtimeFragment.setBack(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_1_FRAGMENT.getKey());
//        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_2_FRAGMENT.getKey(), runtimeFragment);
//
//        runtimeFragment = new Fragment();
//        runtimeFragment.setType(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_3_FRAGMENT.getKey());
//        runtimeFragment.setBack(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_2_FRAGMENT.getKey());
//        runtimeActivity.addFragment(Fragments.CWP_WALLET_DEVELOPER_TOOL_LOG_LEVEL_3_FRAGMENT.getKey(), runtimeFragment);

        listSubApp.put(runtimeSubApp.getPublicKey(), runtimeSubApp);
    }

    private void createChatSubAppNavigationStructure() {

        RuntimeSubApp chtChat;
        Activity runtimeActivity;
        TitleBar runtimeTitleBar;
        StatusBar statusBar;
        Fragment runtimeFragment;
        TabStrip runtimeTabStrip;
        Tab runtimeTab;



        chtChat = new RuntimeSubApp();
        chtChat.setType(SubApps.CHT_CHAT);
        String chatPublicKey = "public_key_cht_chat";
        chtChat.setPublicKey(chatPublicKey);
        listSubApp.put(chtChat.getPublicKey(), chtChat);

        //chtChat.setStartActivity(Activities.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT);



        //Activity Chat
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_OPEN_CHATLIST);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_OPEN_CHATLIST.getCode());
        chtChat.addPosibleStartActivity(Activities.CHT_CHAT_OPEN_CHATLIST);
        //chtChat.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0072bb");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Chats");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT.getKey());


        //Menu Tabs
        runtimeTabStrip = new TabStrip();
        runtimeTabStrip.setTabsColor("#d07b62");
        runtimeTabStrip.setTabsTextColor("#FFFFFF");
        runtimeTabStrip.setTabsIndicateColor("#b46a54");
        runtimeActivity.setTabStrip(runtimeTabStrip);

        //Tabs Chats
        runtimeTab = new Tab();
        runtimeTab.setLabel("Chats");
        runtimeTab.setFragment(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT.getKey());
        runtimeTabStrip.addTab(runtimeTab);

        //Tabs Contacts
        runtimeTab = new Tab();
        runtimeTab.setLabel("Contacts");
        runtimeTab.setFragment(Fragments.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT.getKey());

        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT.getKey(), runtimeFragment);
        runtimeTabStrip.addTab(runtimeTab);

        chtChat.addActivity(runtimeActivity);
        listSubApp.put(chtChat.getPublicKey(), chtChat);
/*
        // Activity: Contacts
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT.getCode());
        runtimeActivity.setBackPublicKey(chatPublicKey);
        runtimeActivity.setColor("#FF0B46F0");

        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Contacts");
        runtimeTitleBar.setLabelSize(20);
        runtimeTitleBar.setTitleColor("#ffffff");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#0072bb");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT.getKey());
        chtChat.addActivity(runtimeActivity);
   */
/*
        // Activity: Edit Contact
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_EDIT_CONTACT_FRAGMENT);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getCode());
        runtimeActivity.setBackActivity(Activities.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT);
        runtimeActivity.setBackPublicKey(chatPublicKey);
        chtChat.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Edit Contact");

        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getKey());
*/
        // Activity: Contact Detail
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_OPEN_CONTACT_DETAIL);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_OPEN_CONTACT_DETAIL.getCode());
        runtimeActivity.setBackActivity(Activities.CHT_CHAT_OPEN_CONTACTLIST);
        runtimeActivity.setBackPublicKey(chatPublicKey);
        chtChat.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("View Contact");

        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CONTACT_DETAIL_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CONTACT_DETAIL_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEN_CONTACT_DETAIL_FRAGMENT.getKey());

        // Activity: Edit Contact
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_EDIT_CONTACT);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_EDIT_CONTACT.getCode());
        runtimeActivity.setBackActivity(Activities.CHT_CHAT_OPEN_CONTACTLIST);
        runtimeActivity.setBackPublicKey(chatPublicKey);
        chtChat.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Edit Contact");

        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_EDIT_CONTACT_FRAGMENT.getKey());

/*
        // Activity: Connections
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_OPEN_CONNECTIONLIST_FRAGMENT);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_OPEN_CONNECTIONLIST_FRAGMENT.getCode());
        runtimeActivity.setBackActivity(Activities.CHT_CHAT_OPEN_CONTACTLIST_TAB_FRAGMENT);
        runtimeActivity.setBackPublicKey(chatPublicKey);
        chtChat.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Connections");

        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CONNECTIONLIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CONNECTIONLIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEN_CONNECTIONLIST_FRAGMENT.getKey());

        // Activity: Chat Detail
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_OPEN_CHAT_DETAIL_FRAGMENT);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_OPEN_CHAT_DETAIL_FRAGMENT.getCode());
        runtimeActivity.setBackActivity(Activities.CHT_CHAT_OPEN_CHATLIST_TAB_FRAGMENT);
        runtimeActivity.setBackPublicKey(chatPublicKey);
        chtChat.addActivity(runtimeActivity);

        statusBar = new StatusBar();
        statusBar.setColor("#0e738b");
        runtimeActivity.setStatusBar(statusBar);

        runtimeTitleBar = new TitleBar();
        runtimeTitleBar.setLabel("Edit Contact");

        runtimeTitleBar.setLabelSize(16);
        runtimeTitleBar.setTitleColor("#FFFFFF");
        runtimeTitleBar.setIsTitleTextStatic(true);
        runtimeTitleBar.setColor("#1189a5");
        runtimeActivity.setTitleBar(runtimeTitleBar);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEN_CHAT_DETAIL_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEN_CHAT_DETAIL_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEN_CHAT_DETAIL_FRAGMENT.getKey());

        chtChat.addActivity(runtimeActivity);
        */

        //NEWMIG
        runtimeActivity = new Activity();
        runtimeActivity.setType(Activities.CHT_CHAT_OPEN_MESSAGE_LIST);
        runtimeActivity.setActivityType(Activities.CHT_CHAT_OPEN_MESSAGE_LIST.getCode());
        runtimeActivity.setBackActivity(Activities.CHT_CHAT_OPEN_CHATLIST);
        runtimeActivity.setBackPublicKey(Activities.CHT_CHAT_OPEN_CHATLIST.getCode());
        chtChat.addActivity(runtimeActivity);

        runtimeFragment = new Fragment();
        runtimeFragment.setType(Fragments.CHT_CHAT_OPEM_MESSAGE_LIST_FRAGMENT.getKey());
        runtimeActivity.addFragment(Fragments.CHT_CHAT_OPEM_MESSAGE_LIST_FRAGMENT.getKey(), runtimeFragment);
        runtimeActivity.setStartFragment(Fragments.CHT_CHAT_OPEM_MESSAGE_LIST_FRAGMENT.getKey());

        //



        listSubApp.put(chtChat.getPublicKey(), chtChat);

    }

}
