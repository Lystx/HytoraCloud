package cloud.hytora.common.progressbar;

import java.util.Collection;

public class HytoraProgressBar extends ProgressBar{

    public HytoraProgressBar(ProgressBarStyle style) {
        super(style, 100);

        this.setAppendProgress(false);
        this.setPrintAutomatically(true);
        this.setExpandingAnimation(true);
    }

    public HytoraProgressBar(ProgressBarStyle style, int maxSize, Collection<?> entries) {
        this(style);
        this.setSizeEntryBased(maxSize, entries.size());
    }
    public HytoraProgressBar(ProgressBarStyle style, int maxSize, Object[] entries) {
        this(style);
        this.setSizeEntryBased(maxSize, entries.length);
    }
}
