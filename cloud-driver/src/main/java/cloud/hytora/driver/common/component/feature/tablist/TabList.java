package cloud.hytora.driver.common.component.feature.tablist;

import cloud.hytora.driver.common.component.base.TextBased;

public interface TabList extends TextBased<TabList> {

    /**
     * The header of this tab
     */
    String getHeader();

    /**
     * The footer of this tab
     */
    String getFooter();
}
