package lu.kbra.modelizer_next.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lu.kbra.modelizer_next.bootstrap.UpdateChannel;

/**
 * Parses and compares Modelizer version strings, including release, snapshot, and nightly variants.
 */
public class VersionComparator implements Comparator<String> {

	/**
	 * Immutable value object for parsed version data.
	 *
	 * @param numbers       values for numbers
	 * @param updateChannel update channel value used by the operation
	 * @param buildNumber   numeric build number value
	 */
	public record ParsedVersion(List<Integer> numbers, UpdateChannel updateChannel, long buildNumber) {

		/**
		 * Builds a debug string for this parsed version.
		 *
		 * @return a debug string for this object
		 */
		@Override
		public final String toString() {
			return numbers.stream().map(v -> Integer.toString(v)).collect(Collectors.joining(".")) + "-" + updateChannel.name() + "-"
					+ buildNumber;
		}

	}

	public static final VersionComparator COMPARATOR = new VersionComparator();
	public static final Comparator<ParsedVersion> PARSED_COMPARATOR = (a, b) -> {
		final int len = Math.max(a.numbers().size(), b.numbers().size());
		for (int i = 0; i < len; i++) {
			final int n1 = i < a.numbers().size() ? a.numbers().get(i) : 0;
			final int n2 = i < b.numbers().size() ? b.numbers().get(i) : 0;
			if (n1 != n2) {
				return Integer.compare(n1, n2);
			}
		}

		if (a.updateChannel() != b.updateChannel()) {
			return Integer.compare(a.updateChannel().ordinal(), b.updateChannel().ordinal());
		}

		return Long.compare(a.buildNumber(), b.buildNumber());
	};

	/**
	 * Compares two values using this comparator's ordering rules.
	 *
	 * @param left  text value for left
	 * @param right text value for right
	 * @return a negative value, zero, or a positive value according to the ordering rules
	 */
	@Override
	public int compare(final String left, final String right) {
		final ParsedVersion a = VersionComparator.parse(left);
		final ParsedVersion b = VersionComparator.parse(right);

		return PARSED_COMPARATOR.compare(a, b);

	}

	/**
	 * Parses the supplied text into the value type used by this class.
	 *
	 * @param version text value for version
	 * @return the parsed value
	 */
	public static ParsedVersion parse(final String version) {
		if (version == null || version.isBlank()) {
			return new ParsedVersion(List.of(0), UpdateChannel.NIGHTLY, 0L);
		}

		final String normalized = version.trim().startsWith("v") || version.trim().startsWith("V") ? version.trim().substring(1)
				: version.trim();
		final String[] tokens = normalized.split("-");
		final List<Integer> numbers = new ArrayList<>();
		for (final String part : tokens[0].split("\\.")) {
			if (part.isBlank()) {
				continue;
			}
			numbers.add(Integer.parseInt(part));
		}
		if (numbers.isEmpty()) {
			numbers.add(0);
		}

		int channelIndex = -1;
		int channelRank = UpdateChannel.CHANNEL_RELEASE;
		for (int i = 1; i < tokens.length; i++) {
			final int candidate = VersionComparator.parseChannelRank(tokens[i]);
			if (candidate >= 0) {
				channelIndex = i;
				channelRank = candidate;
			}
		}

		long buildNumber = 0L;
		if (channelIndex >= 0) {
			final StringBuilder digits = new StringBuilder();
			for (int i = channelIndex + 1; i < tokens.length; i++) {
				for (final char c : tokens[i].toCharArray()) {
					if (Character.isDigit(c)) {
						digits.append(c);
					}
				}
			}
			if (!digits.isEmpty()) {
				buildNumber = Long.parseLong(digits.toString());
			}
		}

		return new ParsedVersion(numbers, UpdateChannel.byId(channelRank), buildNumber);
	}

	/**
	 * Parses the channel rank from the supplied input.
	 *
	 * @param token text value for token
	 * @return the parsed channel rank
	 */
	private static int parseChannelRank(final String token) {
		return switch (token.toUpperCase(Locale.ROOT)) {
		case "NIGHTLY" -> UpdateChannel.CHANNEL_NIGHTLY;
		case "SNAPSHOT" -> UpdateChannel.CHANNEL_SNAPSHOT;
		case "RELEASE" -> UpdateChannel.CHANNEL_RELEASE;
		default -> -1;
		};
	}

}
