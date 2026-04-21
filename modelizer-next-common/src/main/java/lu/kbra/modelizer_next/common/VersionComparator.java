package lu.kbra.modelizer_next.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class VersionComparator implements Comparator<String> {

	private record ParsedVersion(List<Integer> numbers, int channelRank, long buildNumber) {
	}

	private static final int CHANNEL_NIGHTLY = 0;
	private static final int CHANNEL_SNAPSHOT = 1;

	private static final int CHANNEL_RELEASE = 2;

	public static final VersionComparator COMPARATOR = new VersionComparator();

	@Override
	public int compare(final String left, final String right) {
		final ParsedVersion a = this.parse(left);
		final ParsedVersion b = this.parse(right);

		final int len = Math.max(a.numbers().size(), b.numbers().size());
		for (int i = 0; i < len; i++) {
			final int n1 = i < a.numbers().size() ? a.numbers().get(i) : 0;
			final int n2 = i < b.numbers().size() ? b.numbers().get(i) : 0;
			if (n1 != n2) {
				return Integer.compare(n1, n2);
			}
		}

		if (a.channelRank() != b.channelRank()) {
			return Integer.compare(a.channelRank(), b.channelRank());
		}

		return Long.compare(a.buildNumber(), b.buildNumber());
	}

	private ParsedVersion parse(final String version) {
		if (version == null || version.isBlank()) {
			return new ParsedVersion(List.of(0), VersionComparator.CHANNEL_RELEASE, 0L);
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
		int channelRank = VersionComparator.CHANNEL_RELEASE;
		for (int i = 1; i < tokens.length; i++) {
			final int candidate = this.parseChannelRank(tokens[i]);
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

		return new ParsedVersion(numbers, channelRank, buildNumber);
	}

	private int parseChannelRank(final String token) {
		return switch (token.toUpperCase(Locale.ROOT)) {
		case "NIGHTLY" -> VersionComparator.CHANNEL_NIGHTLY;
		case "SNAPSHOT" -> VersionComparator.CHANNEL_SNAPSHOT;
		case "RELEASE" -> VersionComparator.CHANNEL_RELEASE;
		default -> -1;
		};
	}

}
