package org.bitrepository.protocol.security;

import java.io.ByteArrayInputStream;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bitrepository.settings.collectionsettings.OperationPermission;
import org.bitrepository.settings.collectionsettings.PermissionSet;
import org.bitrepository.settings.collectionsettings.Permission;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold the concept of permissions used in the Bitrepository.
 * The class contains functionality to:
 * - Hold the correlation between certificates and the permissions related to them.
 * - Test if a certificate has a requested permission
 * - Retreive a certificate from the store.
 */
public class PermissionStore {

    private final Logger log = LoggerFactory.getLogger(PermissionStore.class);
    private Map<CertificateID, CertificatePermission> permissionMap;

    /**
     * Public constructor, initializes the store. 
     */
    public PermissionStore() {
        permissionMap = new HashMap<CertificateID, CertificatePermission>();
        Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
    }
    
    /**
     * Load permissions and certificates into the store based.
     * @param PermissionSet the PermissionSet from CollectionSettings.
     * @throws CertificateException in case a bad certificate data in PermissionSet.   
     */
    public void loadPermissions(PermissionSet permissions) throws CertificateException {
        if(permissions != null) {
            for(Permission permission : permissions.getPermission()) {
                if(permission.getOperationPermission() != null) {
                    ByteArrayInputStream bs = new ByteArrayInputStream(permission.getCertificate());
                    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bs);
                    CertificateID certID = new CertificateID(certificate.getIssuerX500Principal(), certificate.getSerialNumber());
                    CertificatePermission certificatePermission = new CertificatePermission(certificate, 
                            permission.getOperationPermission());
                    permissionMap.put(certID, certificatePermission);
                }
            }
        } else {
            log.info("The provided PermissionSet was null");
        }
    }
    
    /**
     * Retrieve the certificate based on the signerId.
     * @param SignerId the identification data of the certificate to retrieve  
     * @return X509Certificate the certificate represented by the SignerId
     * @throws PermissionStoreException if no certificate can be found based on the SignerId 
     */
    public X509Certificate getCeritificate(SignerId signer) throws PermissionStoreException {
        CertificateID certificateID = new CertificateID(signer.getIssuer(), signer.getSerialNumber());
        CertificatePermission permission = permissionMap.get(certificateID);
        if(permission != null) {
            return permission.getCertificate();
        } else {
            throw new PermissionStoreException("Failed to find certificate for the requested signer:" + certificateID.toString());
        }
    }
    
    /**
     * Check to see if a certificate has the specified permission. The certificate is identified based 
     * on the SignerId of the signature. 
     * @return true if the requested permission is present for the certificate belonging to the signer, otherwise false.
     * @throws PermissionStoreException in case no certificate and permission set can be found for the provided signer.
     */
    public boolean checkPermission(SignerId signer, OperationPermission permission) throws PermissionStoreException {
        CertificateID certificateID = new CertificateID(signer.getIssuer(), signer.getSerialNumber());
        CertificatePermission certificatePermission = permissionMap.get(certificateID);
        if(certificatePermission == null) {
            throw new PermissionStoreException("Failed to find certificate and permissions for the requested signer: " +
                    certificateID.toString());
        } else {
            return certificatePermission.hasPermission(permission);
        }
    }
    
    /**
     * Class to contain a X509Certificate and the permissions associated with it.    
     */
    private final class CertificatePermission {
        private Set<OperationPermission> permissions;
        private final X509Certificate certificate;
            
        public CertificatePermission(X509Certificate certificate, Collection<OperationPermission> permissions) {
            this.permissions = new HashSet<OperationPermission>();
            this.permissions.addAll(permissions);
            this.certificate = certificate;
        }
        
        public boolean hasPermission(OperationPermission permission) {
            return permissions.contains(permission);
        }
        
        public final X509Certificate getCertificate() {
            return certificate;
        }   
    }
}
