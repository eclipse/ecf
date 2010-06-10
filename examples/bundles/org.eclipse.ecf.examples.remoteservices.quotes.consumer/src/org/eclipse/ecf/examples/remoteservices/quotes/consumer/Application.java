package org.eclipse.ecf.examples.remoteservices.quotes.consumer;

import java.lang.reflect.Method;

import org.eclipse.ecf.services.quotes.QuoteService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Application implements IApplication {

	private ConsumerUI ui;

	@Override
	public Object start(IApplicationContext context) throws Exception {

		System.out.println("Started");

		ui = new ConsumerUI(null);
		addListener();
		ui.main(null);

		return null;
	}

	@Override
	public void stop() {
		System.out.println("Stopped");

	}

	private void addListener() {
		Activator.getContext().addServiceListener(new ServiceListener() {

			@Override
			public void serviceChanged(final ServiceEvent event) {

				/*
				 * Do only for registrations
				 */
				if (event.getType() == ServiceEvent.REGISTERED) {

					try {
						Object obj = Activator.getContext().getService(event.getServiceReference());

						/*
						 * Post all the information we have on this service
						 */
						fillInfo(event.getServiceReference(), obj);

						/*
						 * If we know this service
						 */
						if (obj instanceof QuoteService) {
							final QuoteService service = (QuoteService) Activator.getContext().getService(
									event.getServiceReference());

							final String sLabel = service.getServiceDescription();
							final String sQuote = service.getRandomQuote();

							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									ui.getLabel().setText(sLabel);
									ui.getStyledText().setText(sQuote);
									ui.getDispatcher().setValue(1);
									ui.redraw();

								}
							});

						}
					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								ui.getLabel().setText(e.getLocalizedMessage() + "");
								ui.getStyledText().setText("");
								ui.getDispatcher().setValue(-1);
								ui.redraw();

							}
						});
					}

				}
			}

			private void fillInfo(ServiceReference s, Object obj) {
				final StringBuffer infoBuf = new StringBuffer();
				infoBuf.append("ServiceReference Info\n\t");
				infoBuf.append("Bundle:\t").append(s.getBundle().getSymbolicName()).append("\n\n\t");
				for (String key : s.getPropertyKeys())
					if (s.getProperty(key) instanceof Object[])
						for (int i = 0; i < ((Object[]) s.getProperty(key)).length; i++)
							if (i == 0)
								infoBuf.append("Property:\t").append(key).append("=")
										.append(((Object[]) s.getProperty(key))[i].toString()).append("\n\t");
							else
								infoBuf.append("\t\t\t").append(key).append("=")
										.append(((Object[]) s.getProperty(key))[i]).append("\n\t");
					else
						infoBuf.append("Property:\t").append(key).append("=").append(s.getProperty(key)).append("\n\t");
				infoBuf.append("\nService Info\n\t");
				infoBuf.append("Class:\t").append(obj.getClass().getName()).append("\n\t");
				infoBuf.append("\nImplements").append("\n\t");
				for (Class<?> i : obj.getClass().getInterfaces())
					infoBuf.append("Class:\t").append(i.getName()).append("\n\t");
				infoBuf.append("\nSupers").append("\n\t");
				Class<?> sc = obj.getClass().getSuperclass();
				while (sc != null && sc != Object.class) {
					infoBuf.append("Class:\t").append(sc.getName()).append("\n\t");
					sc = sc.getClass().getSuperclass();
				}
				infoBuf.append("\nMethods").append("\n\t");
				for (Method m : obj.getClass().getMethods()) {
					infoBuf.append("Method:\t").append("(").append(m.getReturnType()).append(") ").append(m.getName())
							.append("(");
					for (Class<?> c : m.getParameterTypes())
						infoBuf.append(c.getSimpleName()).append(" ");
					infoBuf.append(")\n\t");
				}

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						ui.getInfo().setText(infoBuf.toString());

					}
				});
			}
		});
	}
}
