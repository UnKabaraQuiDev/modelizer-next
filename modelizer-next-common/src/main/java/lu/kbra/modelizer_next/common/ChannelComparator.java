package lu.kbra.modelizer_next.common;

import java.util.Comparator;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public class ChannelComparator implements Comparator<String> {

	public static final ChannelComparator COMPARATOR = new ChannelComparator();
	public static final Comparator<ParsedVersion> PARSED_COMPARATOR = (a, b) -> Integer.compare(a.updateChannel().ordinal(),
			b.updateChannel().ordinal());

	@Override
	public int compare(final String left, final String right) {
		final ParsedVersion a = VersionComparator.parse(left);
		final ParsedVersion b = VersionComparator.parse(right);

		return PARSED_COMPARATOR.compare(a, b);
	}

}
