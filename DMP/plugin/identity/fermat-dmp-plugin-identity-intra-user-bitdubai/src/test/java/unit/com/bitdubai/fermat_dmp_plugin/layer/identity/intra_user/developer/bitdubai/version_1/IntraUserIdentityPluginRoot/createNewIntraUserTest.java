package unit.com.bitdubai.fermat_dmp_plugin.layer.identity.intra_user.developer.bitdubai.version_1.IntraUserIdentityPluginRoot;

import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTable;
import com.bitdubai.fermat_api.layer.dmp_identity.intra_user.exceptions.CantCreateNewIntraUserException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginFileSystem;
import com.bitdubai.fermat_dmp_plugin.layer.identity.intra_user.developer.bitdubai.version_1.IntraUserIdentityPluginRoot;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.pip_user.device_user.interfaces.DeviceUserManager;
import com.googlecode.catchexception.CatchException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by angel on 20/8/15.
 */

@RunWith(MockitoJUnitRunner.class)
public class createNewIntraUserTest{

    @Mock
    private Database mockDatabase;

    @Mock
    private PluginDatabaseSystem mockPluginDatabaseSystem;

    @Mock
    private PluginFileSystem mockPluginFileSystem;

    @Mock
    private DeveloperDatabaseTable developerDatabaseTable;

    @Mock
    private DeviceUserManager deviceUserManager;

    @Mock
    private ErrorManager errorManager;

    private IntraUserIdentityPluginRoot pluginRoot;

    @Test
    public void createNewException() throws CantOpenDatabaseException, DatabaseNotFoundException, CantCreateNewIntraUserException {

        UUID testOwnerId = UUID.randomUUID();

        pluginRoot = new IntraUserIdentityPluginRoot();

        when(mockPluginDatabaseSystem.openDatabase(any(UUID.class), anyString())).thenReturn(mockDatabase);

        pluginRoot.setPluginDatabaseSystem(mockPluginDatabaseSystem);
        pluginRoot.setPluginFileSystem(mockPluginFileSystem);
        pluginRoot.setId(testOwnerId);
        pluginRoot.setErrorManager(errorManager);
        pluginRoot.setDeviceUserManager(deviceUserManager);

        byte[] profile = new byte[100];

        catchException(pluginRoot).createNewIntraUser("alias", profile);
        assertThat(CatchException.<Exception>caughtException()).isNotNull();

    }
}