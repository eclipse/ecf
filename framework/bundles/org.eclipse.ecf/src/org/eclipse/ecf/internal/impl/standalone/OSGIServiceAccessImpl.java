/*
 * Created on Dec 6, 2004
 *
 */
package org.eclipse.ecf.internal.impl.standalone;

import java.util.Dictionary;

import org.eclipse.ecf.core.IOSGIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class OSGIServiceAccessImpl implements IOSGIService {

    BundleContext context;
    
    public OSGIServiceAccessImpl(BundleContext ctx) {
        super();
        this.context = ctx;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.IOSGIService#getServiceReference(java.lang.String)
     */
    public ServiceReference getServiceReference(String svc) {
        return context.getServiceReference(svc);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.IOSGIService#getService(org.osgi.framework.ServiceReference)
     */
    public Object getService(ServiceReference reference) {
        return context.getService(reference);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.IOSGIService#getServiceReferences(java.lang.String, java.lang.String)
     */
    public ServiceReference[] getServiceReferences(String clazz, String filter)
            throws InvalidSyntaxException {
        return context.getServiceReferences(clazz,filter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.IOSGIService#registerService(java.lang.String[], java.lang.Object, java.util.Dictionary)
     */
    public ServiceRegistration registerService(String[] clazzes,
            Object service, Dictionary properties) {
        return context.registerService(clazzes,service,properties);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.IOSGIService#registerService(java.lang.String, java.lang.Object, java.util.Dictionary)
     */
    public ServiceRegistration registerService(String clazz, Object service,
            Dictionary properties) {
        return context.registerService(clazz,service,properties);
    }

}
