package com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1;

import com.bitdubai.fermat_api.CantStartPluginException;
import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.abstract_classes.AbstractPlugin;
import com.bitdubai.fermat_api.layer.all_definition.common.system.annotations.NeededAddonReference;
import com.bitdubai.fermat_api.layer.all_definition.common.system.annotations.NeededPluginReference;
import com.bitdubai.fermat_api.Plugin;
import com.bitdubai.fermat_api.Service;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.FermatManager;
import com.bitdubai.fermat_api.layer.all_definition.common.system.utils.PluginVersionReference;
import com.bitdubai.fermat_api.layer.all_definition.developer.DatabaseManagerForDevelopers;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabase;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTable;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTableRecord;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperObjectFactory;
import com.bitdubai.fermat_api.layer.all_definition.developer.LogManagerForDevelopers;
//import com.bitdubai.fermat_api.layer.all_definition.enums.Plugins;
import com.bitdubai.fermat_api.layer.all_definition.enums.Addons;
import com.bitdubai.fermat_api.layer.all_definition.enums.Layers;
import com.bitdubai.fermat_api.layer.all_definition.enums.Platforms;
import com.bitdubai.fermat_api.layer.all_definition.enums.ServiceStatus;

import com.bitdubai.fermat_api.layer.all_definition.util.Version;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantCreateDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.DealsWithLogger;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.LogLevel;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.LogManager;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.interfaces.CustomerBrokerPurchaseNegotiation;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.interfaces.CustomerBrokerPurchaseNegotiationManager;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_sale.interfaces.CustomerBrokerSaleNegotiation;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_sale.interfaces.CustomerBrokerSaleNegotiationManager;
import com.bitdubai.fermat_cbp_api.layer.network_service.negotiation_transmission.interfaces.NegotiationTransmissionManager;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.database.CustomerBrokerCloseNegotiationTransactionDatabaseConstants;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.database.CustomerBrokerCloseNegotiationTransactionDatabaseDao;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.database.CustomerBrokerCloseNegotiationTransactionDatabaseFactory;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.database.CustomerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.event_handler.CustomerBrokerCloseServiceEventHandler;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.exceptions.CantInitializeCustomerBrokerCloseNegotiationTransactionDatabaseException;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.structure.CustomerBrokerCloseAgent;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.structure.CustomerBrokerCloseManagerImpl;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.structure.CustomerBrokerClosePurchaseNegotiationTransaction;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.structure.CustomerBrokerCloseSaleNegotiationTransaction;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.DealsWithErrors;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.enums.UnexpectedPluginExceptionSeverity;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.interfaces.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.platform_service.event_manager.interfaces.EventManager;
import com.bitdubai.fermat_pip_api.layer.user.device_user.interfaces.DeviceUserManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Yordin Alayn on 16.09.15.
 */

public class NegotiationTransactionCustomerBrokerClosePluginRoot extends AbstractPlugin implements
        DatabaseManagerForDevelopers,
        LogManagerForDevelopers{

    @NeededAddonReference(platform = Platforms.PLUG_INS_PLATFORM,       layer = Layers.PLATFORM_SERVICE, addon = Addons.ERROR_MANAGER)
    private ErrorManager errorManager;

    @NeededAddonReference(platform = Platforms.PLUG_INS_PLATFORM,       layer = Layers.USER, addon = Addons.DEVICE_USER)
    private DeviceUserManager deviceUserManager;

    @NeededAddonReference(platform = Platforms.OPERATIVE_SYSTEM_API,    layer = Layers.SYSTEM, addon = Addons.PLUGIN_DATABASE_SYSTEM)
    private PluginDatabaseSystem pluginDatabaseSystem;

    @NeededAddonReference(platform = Platforms.OPERATIVE_SYSTEM_API,    layer = Layers.SYSTEM,              addon = Addons.LOG_MANAGER)
    private LogManager logManager;

    @NeededAddonReference(platform = Platforms.PLUG_INS_PLATFORM,       layer = Layers.PLATFORM_SERVICE,    addon = Addons.EVENT_MANAGER)
    private EventManager eventManager;

    /*Represent the dataBase*/
    private Database                                                            dataBase;

    /*Represent DeveloperDatabaseFactory*/
    private CustomerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory   customerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory;

    /*Represent CustomerBrokerNewNegotiationTransactionDatabaseDao*/
    private CustomerBrokerCloseNegotiationTransactionDatabaseDao                customerBrokerCloseNegotiationTransactionDatabaseDao;

    /*Represent Customer Broker New Manager*/
    private CustomerBrokerCloseManagerImpl                                      customerBrokerCloseManagerImpl;

    /*Represent Agent*/
    private CustomerBrokerCloseAgent                                            customerBrokerCloseAgent;

    /*Represent Network Service Negotiation Transmission*/
    private NegotiationTransmissionManager                                      negotiationTransmissionManager;

    /*Represent the Transaction Negotiation Purchase*/
    private CustomerBrokerClosePurchaseNegotiationTransaction                   customerBrokerClosePurchaseNegotiationTransaction;

    /*Represent the Transaction Negotiation Sale*/
    private CustomerBrokerCloseSaleNegotiationTransaction                       customerBrokerCloseSaleNegotiationTransaction;

    /*Represent the Negotiation Purchase*/
    private CustomerBrokerPurchaseNegotiationManager                            customerBrokerPurchaseNegotiationManager;

    /*Represent the Negotiation Sale*/
    private CustomerBrokerSaleNegotiationManager                                customerBrokerSaleNegotiationManager;

    /*Represent the Negotiation Purchase*/
    private CustomerBrokerPurchaseNegotiation                                   customerBrokerPurchaseNegotiation;

    /*Represent the Negotiation Sale*/
    private CustomerBrokerSaleNegotiation                                       customerBrokerSaleNegotiation;

    /*Represent Service Event Handler*/
    private CustomerBrokerCloseServiceEventHandler                              customerBrokerCloseServiceEventHandler;

    static Map<String, LogLevel> newLoggingLevel = new HashMap<String, LogLevel>();

    public NegotiationTransactionCustomerBrokerClosePluginRoot() {
        super(new PluginVersionReference(new Version()));
    }

     /*IMPLEMENTATION Service.*/
     @Override
     public void start() throws CantStartPluginException {
         try {

             //Initialize database
             initializeDb();

             //Initialize Developer Database Factory
             customerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory = new CustomerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId);
             customerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory.initializeDatabase();

             //Initialize Dao
             customerBrokerCloseNegotiationTransactionDatabaseDao = new CustomerBrokerCloseNegotiationTransactionDatabaseDao(pluginDatabaseSystem, pluginId, dataBase);

             //Initialize manager
             customerBrokerCloseManagerImpl = new CustomerBrokerCloseManagerImpl(customerBrokerCloseNegotiationTransactionDatabaseDao);

             //Init event recorder service.
             customerBrokerCloseServiceEventHandler = new CustomerBrokerCloseServiceEventHandler(customerBrokerCloseNegotiationTransactionDatabaseDao,eventManager);
             customerBrokerCloseServiceEventHandler.start();

             //Init monitor Agent
             customerBrokerCloseAgent = new CustomerBrokerCloseAgent(
                     pluginDatabaseSystem,
                     logManager,
                     errorManager,
                     eventManager,
                     pluginId,
                     negotiationTransmissionManager,
                     customerBrokerPurchaseNegotiation,
                     customerBrokerSaleNegotiation,
                     customerBrokerPurchaseNegotiationManager,
                     customerBrokerSaleNegotiationManager

             );
             customerBrokerCloseAgent.start();

             //Startes Service
             this.serviceStatus = ServiceStatus.STARTED;
             System.out.print("-----------------------\n CUSTOMER BROKER CLOSE: SUCCESSFUL START \n-----------------------\n");

         } catch (Exception exception) {
             throw new CantStartPluginException(CantStartPluginException.DEFAULT_MESSAGE,FermatException.wrapException(exception),"Error Starting Customer Broker Close PluginRoot","Unexpected Exception");
         }
     }

    @Override
    public void pause() {
        this.serviceStatus = ServiceStatus.PAUSED;
    }

    @Override
    public void resume() {
        this.serviceStatus = ServiceStatus.STARTED;
    }

    @Override
    public void stop() {
        this.serviceStatus = ServiceStatus.STOPPED;
    }
    /*END IMPLEMENTATION Service.*/

    /*IMPLEMENTATION DatabaseManagerForDevelopers*/
    @Override
    public List<DeveloperDatabase> getDatabaseList(DeveloperObjectFactory developerObjectFactory) {
        return new CustomerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId).getDatabaseList(developerObjectFactory);
    }

    @Override
    public List<DeveloperDatabaseTable> getDatabaseTableList(DeveloperObjectFactory developerObjectFactory, DeveloperDatabase developerDatabase) {
        return new CustomerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId).getDatabaseTableList(developerObjectFactory);
    }

    @Override
    public List<DeveloperDatabaseTableRecord> getDatabaseTableContent(DeveloperObjectFactory developerObjectFactory, DeveloperDatabase developerDatabase, DeveloperDatabaseTable developerDatabaseTable) {
        try{
            return new CustomerBrokerCloseNegotiationTransactionDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId).getDatabaseTableContent(developerObjectFactory, developerDatabaseTable);
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }
    /*END IMPLEMENTATION DatabaseManagerForDevelopers*/

    /*IMPLEMENTATION LogManagerForDevelopers*/
    @Override
    public List<String> getClassesFullPath() {
        List<String> returnedClasses = new ArrayList<String>();
        returnedClasses.add("com.bitdubai.fermat_cbp_plugin.layer.negotiation_transaction.customer_broker_close.developer.bitdubai.version_1.NegotiationTransactionCustomerBrokerClosePluginRoot");
        return returnedClasses;
    }

    @Override
    public void setLoggingLevelPerClass(Map<String, LogLevel> newLoggingLevel) {

        //I will check the current values and update the LogLevel in those which is different
        for (Map.Entry<String, LogLevel> pluginPair : newLoggingLevel.entrySet()) {

            //if this path already exists in the Root.bewLoggingLevel I'll update the value, else, I will put as new
            if (NegotiationTransactionCustomerBrokerClosePluginRoot.newLoggingLevel.containsKey(pluginPair.getKey())) {
                NegotiationTransactionCustomerBrokerClosePluginRoot.newLoggingLevel.remove(pluginPair.getKey());
                NegotiationTransactionCustomerBrokerClosePluginRoot.newLoggingLevel.put(pluginPair.getKey(), pluginPair.getValue());
            } else {
                NegotiationTransactionCustomerBrokerClosePluginRoot.newLoggingLevel.put(pluginPair.getKey(), pluginPair.getValue());
            }
        }
    }

    public static LogLevel getLogLevelByClass(String className) {
        try{
            //sometimes the classname may be passed dynamically with an $moretext. I need to ignore whats after this.
            String[] correctedClass = className.split((Pattern.quote("$")));
            return NegotiationTransactionCustomerBrokerClosePluginRoot.newLoggingLevel.get(correctedClass[0]);
        } catch (Exception e){
            //If I couldn't get the correct logging level, then I will set it to minimal.
            return DEFAULT_LOG_LEVEL;
        }
    }
    /*END IMPLEMENTATION LogManagerForDevelopers*/

    /*PUBLIC METHOD*/
    private void initializeDb() throws CantInitializeCustomerBrokerCloseNegotiationTransactionDatabaseException {
        try {
            dataBase = this.pluginDatabaseSystem.openDatabase(this.pluginId, CustomerBrokerCloseNegotiationTransactionDatabaseConstants.DATABASE_NAME);
        } catch (DatabaseNotFoundException e) {
            try {
                CustomerBrokerCloseNegotiationTransactionDatabaseFactory databaseFactory = new CustomerBrokerCloseNegotiationTransactionDatabaseFactory(pluginDatabaseSystem);
                dataBase = databaseFactory.createDatabase(pluginId, CustomerBrokerCloseNegotiationTransactionDatabaseConstants.DATABASE_NAME);
            } catch (CantCreateDatabaseException f) {
                throw new CantInitializeCustomerBrokerCloseNegotiationTransactionDatabaseException(CantCreateDatabaseException.DEFAULT_MESSAGE, f, "", "There is a problem and i cannot create the database.");
            } catch (Exception z) {
                throw new CantInitializeCustomerBrokerCloseNegotiationTransactionDatabaseException(CantOpenDatabaseException.DEFAULT_MESSAGE, z, "", "Generic Exception.");
            }
        } catch (CantOpenDatabaseException e) {
            throw new CantInitializeCustomerBrokerCloseNegotiationTransactionDatabaseException(CantOpenDatabaseException.DEFAULT_MESSAGE, e, "", "Exception not handled by the plugin, there is a problem and i cannot open the database.");
        } catch (Exception e) {
            throw new CantInitializeCustomerBrokerCloseNegotiationTransactionDatabaseException(CantOpenDatabaseException.DEFAULT_MESSAGE, e, "", "Generic Exception.");
        }
    }
    /*END PUBLIC METHOD*/
}