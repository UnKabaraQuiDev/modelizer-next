package lu.kbra.modelizer_next.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VersionComparator implements Comparator<String> {

	@Override
	public int compare(final String v1, final String v2) {
		final Version a = this.parse(v1);
		final Version b = this.parse(v2);

		// 1. compare main version numbers
		final int len = Math.max(a.numbers.size(), b.numbers.size());
		for (int i = 0; i < len; i++) {
			final int n1 = i < a.numbers.size() ? a.numbers.get(i) : 0;
			final int n2 = i < b.numbers.size() ? b.numbers.get(i) : 0;
			if (n1 != n2) {
				return Integer.compare(n1, n2);
			}
		}

		// 2. compare suffix type (ASCII)
		if (a.type != b.type) {
			return Integer.compare(a.type, b.type);
		}

		// 3. compare date
		return Integer.compare(a.date, b.date);
	}

	private Version parse(final String v) {
		final String[] mainAndSuffix = v.split("-", 2);

		// parse numeric version
		final String[] numParts = mainAndSuffix[0].split("\\.");
		List<Integer> numbers = new ArrayList<>();
		for (final String p : numParts) {
			numbers.add(Integer.parseInt(p));
		}

		int type;
		int date;

		if (mainAndSuffix.length == 2) {
			// format: NIGHTLY20250419
			final String suffix = mainAndSuffix[1];

			if (suffix.startsWith("NIGHTLY")) {
				type = 'N'; // 78
				date = Integer.parseInt(suffix.substring(7));
			} else if (suffix.startsWith("SNAPSHOT")) {
				type = 'S'; // 83
				date = Integer.parseInt(suffix.substring(8));
			} else if (suffix.startsWith("RELEASE")) {
				type = 'R'; // 82
				date = Integer.parseInt(suffix.substring(7));
			} else {
				throw new IllegalArgumentException("Unknown suffix: " + suffix);
			}
		} else {
			// fallback: last two parts = type + date
			final int size = numbers.size();
			type = numbers.get(size - 2);
			date = numbers.get(size - 1);

			numbers = numbers.subList(0, size - 2);
		}

		return new Version(numbers, type, date);
	}

	private static class Version {
		List<Integer> numbers;
		int type;
		int date;

		Version(final List<Integer> numbers, final int type, final int date) {
			this.numbers = numbers;
			this.type = type;
			this.date = date;
		}
	}

}