package com.ctrip.ferriswheel.quarks.util;

import com.ctrip.ferriswheel.quarks.syntax.lr.LR1ParsingTableConstructor;
import com.ctrip.ferriswheel.quarks.syntax.lr.ParsingTable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sqwen on 2016/4/21.
 */
public class BNFTableFactory {

    public static final BNFTableFactory INSTANCE = new BNFTableFactory();

    private Map<String, ParsingTable> map = new HashMap<>();

    private BNFTableFactory(){}

    public ParsingTable getParsingTable(String bnfFile) {
        ParsingTable table = map.get(bnfFile);
        if (table == null) {
            synchronized (map) {
                table = map.get(bnfFile);
                if (table == null) {
                    InputStream is = null;
                    try {
                        LR1ParsingTableConstructor constructor = new LR1ParsingTableConstructor();
                        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(bnfFile);
                        if (is == null) {
                            is = new FileInputStream(bnfFile);
                        }
                        map.put(bnfFile, table = constructor.construct(is));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        if(is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        }
        return table;
    }

}
