package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.bean.Version;
import com.ctrip.ferriswheel.core.bean.Version;

public interface Workbook extends NamedAsset, Iterable<Sheet> {
    Version getVersion();

    void setName(String name);

    int getSheetCount();

    Sheet getSheet(int index);

    Sheet getSheet(String name);

    Sheet addSheet(String name);

    Sheet addSheet(int index, String name);

    void renameSheet(String oldName, String newName);

    void moveSheet(String name, int index);

    Sheet removeSheet(int index);

    Sheet removeSheet(String name);

    void refresh();

    void refresh(boolean force);

    void addListener(ActionListener listener);

    boolean removeListener(ActionListener listener);
}
