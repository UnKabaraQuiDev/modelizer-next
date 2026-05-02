package lu.kbra.modelizer_next.ui.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.App;

public class InfoTab extends JPanel {

	private static final long serialVersionUID = 4519272106455377834L;

	private static final List<LicenseItem> LICENSES = List.of(
			new LicenseItem("Modelizer Next App, Boostrap & Common", "General Public Licence 3.0", "Application, updater and common code."),
			new LicenseItem("PCLib Common", "General Public Licence 3.0", "Common utility library used by the app."),
			new LicenseItem("PCLib Datastruct", "General Public Licence 3.0", "Data structure helper library used by the app."),
			new LicenseItem("FlatLaf", "Apache License 2.0", "Swing look and feel library."),
			new LicenseItem("Jackson Databind", "Apache License 2.0", "JSON serialization and deserialization."),
			new LicenseItem("Jackson Datatype JSR310", "Apache License 2.0", "Java time support for Jackson."),
			new LicenseItem("Apache Batik SVGGen", "Apache License 2.0", "SVG generation support."),
			new LicenseItem("Apache Batik DOM", "Apache License 2.0", "SVG DOM support."),
			new LicenseItem("Modern Docking Single App", "MIT License", "Docking layout support."),
			new LicenseItem("Modern Docking UI", "MIT License", "Docking user interface components."));

	public InfoTab() {
		super(new BorderLayout());
		this.setOpaque(false);
		this.add(this.createInfoPage(), BorderLayout.CENTER);
	}

	private JScrollPane createInfoPage() {
		final JPanel content = HelpUi.createPageContent();

		content.add(HelpUi.createHeading("Additional Information"));
		content.add(Box.createVerticalStrut(18));
		content.add(this.createAboutCard());
		content.add(Box.createVerticalStrut(18));
		content.add(this.createBuildCard());
		content.add(Box.createVerticalStrut(18));
		content.add(this.createBootstrapBuildCard());
		content.add(Box.createVerticalStrut(18));
		content.add(this.createLinksCard());
		content.add(Box.createVerticalStrut(18));
		content.add(this.createLicensesCard());

		return HelpUi.createScrollPane(content);
	}

	private JPanel createLinksButtonPanel() {
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		buttons.setOpaque(false);
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttons.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);
		return buttons;
	}

	private JComponent createAboutCard() {
		final HelpUi.CardPanel card = this.createCard();

		card.add(HelpUi.cardTitle("About the app"), BorderLayout.NORTH);

		final JPanel body = this.createVerticalBody();
		body.add(HelpUi.paragraph(
				"Modelizer Next is a desktop app for creating and editing model diagrams. You can add tables, fields, comments, and links, then save your work or export it as an image."));
		body.add(Box.createVerticalStrut(12));
//		body.add(HelpUi.infoRow("App", HelpUi.fallback(App.NAME, "Modelizer Next")));
//		body.add(HelpUi.infoRow("Description", HelpUi.fallback(App.DESCRIPTION, "No description embedded in this build.")));
//		body.add(HelpUi.infoRow("Distributor", HelpUi.fallback(App.DISTRIBUTOR, "Not embedded in this build.")));

		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private JComponent createBuildCard() {
		final HelpUi.CardPanel card = this.createCard();

		card.add(HelpUi.cardTitle("Build information"), BorderLayout.NORTH);

		final JPanel body = this.createVerticalBody();
		body.add(HelpUi.infoRow("Version", HelpUi.fallback(App.VERSION, "Not embedded")));
		body.add(HelpUi.infoRow("Revision", HelpUi.fallback(this.appJsonText("revision"), "Not embedded")));
		body.add(HelpUi.infoRow("Portable", Boolean.toString(App.PORTABLE)));
		body.add(HelpUi.infoRow("Distributor", HelpUi.fallback(App.DISTRIBUTOR, "Not embedded in this build.")));

		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private JComponent createBootstrapBuildCard() {
		final HelpUi.CardPanel card = this.createCard();

		card.add(HelpUi.cardTitle("Bootstrap build information"), BorderLayout.NORTH);

		final JPanel body = this.createVerticalBody();
//		body.add(HelpUi.infoRow("Entry point", HelpUi.fallback(App.ENTRY_POINT, "Not embedded")));
		body.add(HelpUi.infoRow("Java runtime",
				this.systemProperty("java.runtime.name") + " " + this.systemProperty("java.runtime.version")));
		body.add(HelpUi.infoRow("Java VM", this.systemProperty("java.vm.name") + " " + this.systemProperty("java.vm.version")));
		body.add(HelpUi.infoRow("Java vendor", this.systemProperty("java.vendor")));
		body.add(HelpUi.infoRow("OS",
				this.systemProperty("os.name") + " " + this.systemProperty("os.version") + " " + this.systemProperty("os.arch")));
		body.add(HelpUi.infoRow("Working directory", this.systemProperty("user.dir")));
		body.add(HelpUi.infoRow("App directory", App.getAppDirectory().getAbsolutePath()));

		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private JComponent createLinksCard() {
		final HelpUi.CardPanel card = this.createCard();

		card.add(HelpUi.cardTitle("Links"), BorderLayout.NORTH);

		final JPanel body = this.createVerticalBody();

		final JPanel buttons = this.createLinksButtonPanel();
		buttons.add(HelpUi.linkButton("Website", App.WEBSITE_URL));
		buttons.add(HelpUi.linkButton("Issues", App.ISSUES_URL));
		buttons.add(HelpUi.linkButton("About the author", App.AUTHOR_WEBSITE_URL));

		body.add(buttons);
//		body.add(Box.createVerticalStrut(12));
//		body.add(HelpUi.infoRow("Website", HelpUi.fallback(App.WEBSITE_URL, "Not embedded")));
//		body.add(HelpUi.infoRow("Issues", HelpUi.fallback(App.ISSUES_URL, "Not embedded")));
//		body.add(HelpUi.infoRow("About the author", AUTHOR_URL));

		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private JComponent createLicensesCard() {
		final HelpUi.CardPanel card = this.createCard();

		card.add(HelpUi.cardTitle("Licenses"), BorderLayout.NORTH);

		final JPanel body = this.createVerticalBody();
//		body.add(HelpUi.paragraph("This list covers the app and the main runtime dependencies used by this build."));
		body.add(Box.createVerticalStrut(12));

		final JPanel list = new JPanel(new GridBagLayout());
		list.setOpaque(false);
		list.setAlignmentX(Component.LEFT_ALIGNMENT);

		for (int index = 0; index < LICENSES.size(); index++) {
			final LicenseItem item = LICENSES.get(index);
			final JComponent row = this.createLicenseRow(item);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = index;
			constraints.weightx = 1.0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.insets = new Insets(0, 0, index == LICENSES.size() - 1 ? 0 : 10, 0);
			list.add(row, constraints);
		}

		body.add(list);

		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private JComponent createLicenseRow(final LicenseItem item) {
		final JPanel row = new JPanel();
		row.setOpaque(false);
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		final JLabel title = HelpUi.groupTitle(item.name() + " — " + item.license());
		title.setAlignmentX(Component.LEFT_ALIGNMENT);

		final javax.swing.JTextArea description = HelpUi.paragraph(item.description());
		description.setAlignmentX(Component.LEFT_ALIGNMENT);

		row.add(title);
		row.add(Box.createVerticalStrut(3));
		row.add(description);

		return row;
	}

	private HelpUi.CardPanel createCard() {
		final HelpUi.CardPanel card = new HelpUi.CardPanel(new BorderLayout(0, 14));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);
		card.setBorder(HelpUi.CARD_BORDER);
		return card;
	}

	private JPanel createVerticalBody() {
		final JPanel body = new JPanel();
		body.setOpaque(false);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setAlignmentX(Component.LEFT_ALIGNMENT);
		body.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);
		return body;
	}

	private String appJsonText(final String key) {
		final JsonNode json = App.JSON;
		if (json == null) {
			return "";
		}

		return json.path(key).asText("");
	}

	private String packageText(final PackageValueReader reader) {
		final Package appPackage = InfoTab.class.getPackage();
		if (appPackage == null) {
			return "";
		}

		final String value = reader.read(appPackage);
		return value == null ? "" : value;
	}

	private String systemProperty(final String key) {
		return System.getProperty(key, "Unknown");
	}

	private interface PackageValueReader {

		String read(Package appPackage);
	}

	private record LicenseItem(String name, String license, String description) {
	}

}