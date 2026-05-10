package lu.kbra.modelizer_next.bootstrap.config;

import lu.kbra.modelizer_next.bootstrap.UpdateChannel;

/**
 * Persisted bootstrap configuration, including the selected update channel and installed app state.
 */
public class BootstrapConfiguration {

	private UpdateChannel updateChannel = UpdateChannel.RELEASE;
	private boolean autoCheckUpdates = true;

	/**
	 * Returns the update channel.
	 * @return the update channel
	 */
	public UpdateChannel getUpdateChannel() {
		return this.updateChannel == null ? UpdateChannel.RELEASE : this.updateChannel;
	}

	/**
	 * Checks whether auto check updates is enabled or applies.
	 * @return {@code true} if auto check updates is enabled or applies; otherwise {@code false}
	 */
	public boolean isAutoCheckUpdates() {
		return this.autoCheckUpdates;
	}

	/**
	 * Sets the auto check updates.
	 * @param autoCheckUpdates whether auto check updates is enabled
	 */
	public void setAutoCheckUpdates(final boolean autoCheckUpdates) {
		this.autoCheckUpdates = autoCheckUpdates;
	}

	/**
	 * Sets the update channel.
	 * @param updateChannel update channel value used by the operation
	 */
	public void setUpdateChannel(final UpdateChannel updateChannel) {
		this.updateChannel = updateChannel == null ? UpdateChannel.RELEASE : updateChannel;
	}
}
