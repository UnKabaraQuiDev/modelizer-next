package lu.kbra.modelizer_next.bootstrap;

public class BootstrapConfiguration {

	private UpdateChannel updateChannel = UpdateChannel.RELEASE;
	private boolean autoCheckUpdates = true;

	public UpdateChannel getUpdateChannel() {
		return this.updateChannel == null ? UpdateChannel.RELEASE : this.updateChannel;
	}

	public boolean isAutoCheckUpdates() {
		return this.autoCheckUpdates;
	}

	public void setAutoCheckUpdates(final boolean autoCheckUpdates) {
		this.autoCheckUpdates = autoCheckUpdates;
	}

	public void setUpdateChannel(final UpdateChannel updateChannel) {
		this.updateChannel = updateChannel == null ? UpdateChannel.RELEASE : updateChannel;
	}
}
