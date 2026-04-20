package lu.kbra.modelizer_next.bootstrap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class BootstrapVersionComparator implements Comparator<String> {

	private record ParsedVersion(List<Integer> numbers, int type, int date) {
	}

	static final BootstrapVersionComparator COMPARATOR = new BootstrapVersionComparator();

	@Override
	public int compare(final String left, final String right) {
		final ParsedVersion a = this.parse(left);
		final ParsedVersion b = this.parse(right);
		final int max = Math.max(a.numbers.size(), b.numbers.size());
		for (int i = 0; i < max; i++) {
			final int la = i < a.numbers.size() ? a.numbers.get(i) : 0;
			final int lb = i < b.numbers.size() ? b.numbers.get(i) : 0;
			if (la != lb) {
				return Integer.compare(la, lb);
			}
		}
		if (a.type != b.type) {
			return Integer.compare(a.type, b.type);
		}
		return Integer.compare(a.date, b.date);
	}

	private ParsedVersion parse(final String version) {
		if (version == null || version.isBlank()) {
			return new ParsedVersion(List.of(0), 0, 0);
		}
		final String normalized = version.startsWith("v") || version.startsWith("V") ? version.substring(1) : version;
		final String[] mainAndSuffix = normalized.split("-", 2);
		final List<Integer> numbers = new ArrayList<>();
		for (final String part : mainAndSuffix[0].split("\\.")) {
			numbers.add(Integer.parseInt(part));
		}
		int type = 0;
		int date = 0;
		if (mainAndSuffix.length == 2) {
			final String suffix = mainAndSuffix[1].toUpperCase();
			if (suffix.startsWith("NIGHTLY")) {
				type = 'N';
				date = this.parseOptionalDate(suffix.substring("NIGHTLY".length()));
			} else if (suffix.startsWith("SNAPSHOT")) {
				type = 'S';
				date = this.parseOptionalDate(suffix.substring("SNAPSHOT".length()));
			} else if (suffix.startsWith("RELEASE")) {
				type = 'R';
				date = this.parseOptionalDate(suffix.substring("RELEASE".length()));
			}
		}
		return new ParsedVersion(numbers, type, date);
	}

	private int parseOptionalDate(final String raw) {
		if (raw == null || raw.isBlank()) {
			return 0;
		}
		final String digits = raw.replaceAll("[^0-9]", "");
		return digits.isBlank() ? 0 : Integer.parseInt(digits);
	}
}
