package be.hehehe.supersonic;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import be.hehehe.supersonic.service.IconService;

@Singleton
public class SupersonicTray {

	@Inject
	IconService iconService;

	public boolean addTray(final Supersonic supersonic) {
		if (!SystemTray.isSupported()) {
			return false;
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(iconService
				.getIcon("supersonic").getImage());
		final SystemTray tray = SystemTray.getSystemTray();

		MenuItem restoreItem = new MenuItem("Restore");
		restoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				supersonic.showSupersonic();
			}
		});
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		popup.add(restoreItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					System.out.println(supersonic.getState());
					if (supersonic.isVisible()) {
						supersonic.hideSupersonic();
					} else {
						supersonic.showSupersonic();
					}

				}
			}
		});

		boolean trayAdded = false;
		try {
			tray.add(trayIcon);
			trayAdded = true;
		} catch (Exception e) {
			trayAdded = false;
		}
		return trayAdded;
	}
}
