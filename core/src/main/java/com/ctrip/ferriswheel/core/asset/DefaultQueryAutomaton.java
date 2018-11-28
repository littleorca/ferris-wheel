package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.action.ExecuteQuery;
import com.ctrip.ferriswheel.core.action.ExecuteQuery;
import com.ctrip.ferriswheel.core.bean.DefaultDataQuery;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.intf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultQueryAutomaton extends AbstractTableAutomaton implements QueryAutomaton {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultQueryAutomaton.class);
    private final DefaultQueryTemplate template;
    private transient Map<String, Variant> parameters;
    private transient DataQuery query;

    DefaultQueryAutomaton(AssetManager assetManager, TableAutomatonInfo.QueryAutomatonInfo automatonInfo) {
        super(assetManager);
        this.template = new DefaultQueryTemplate(assetManager, automatonInfo.getTemplate());
        this.parameters = automatonInfo.getParameters() == null ? Collections.emptyMap() : new LinkedHashMap<>(automatonInfo.getParameters());
        this.query = automatonInfo.getQuery() == null ? null : new DefaultDataQuery(automatonInfo.getQuery());

        bindChild(template);
//        parameters = Collections.emptyMap();
    }

    @Override
    public void query(Map<String, Variant> userParameters) {
        handleAction(new ExecuteQuery(
                getTable().getSheet().getName(),
                getTable().getName(),
                userParameters));
    }

    public void handleAction(ExecuteQuery executeQuery) {
        getTable().publicly(executeQuery, () -> {
            if (executeQuery.getParams() == null) {
                this.parameters = Collections.emptyMap();
            } else {
                this.parameters = executeQuery.getParams();
            }
            this.query = null; // clear old query

            getTable().getWorkbook().refreshIfNeeded();
        });
    }

    /**
     * Do not call this method manually. this method exists for workbook
     */
    @Override
    public void execute(boolean forceUpdate) {
        if (!forceUpdate && query != null &&
                getLastUpdateSequenceNumber() > template.getLastUpdateSequenceNumber()) {
            return;
        }

        DefaultWorkbook wb = parent(DefaultWorkbook.class);

        if (wb.isForceRefresh()) {
            parameters = Collections.emptyMap();
        }
        this.query = template.renderQuery(parameters);
        ProviderManager pm = wb.getEnvironment().getProviderManager();
        DataProvider provider = pm.getProvider(query);

        try {
            if (provider == null) {
                throw new RuntimeException("Unable to find data provider for query scheme: "
                        + query.getScheme());
            }

            // execute query
            DataSet dataSet = provider.execute(query);

            // fill table
            DefaultTable table = getTable();
            fillTable(table, dataSet);
            setLastUpdateSequenceNumber(parent(DefaultWorkbook.class).nextSequenceNumber());
            getTable().getWorkbook().onAutomatonExecuted(getTable());

        } catch (IOException e) {
            LOG.warn("Failed to execute query.", e);
            // TODO mark error
            clearTable();
        } catch (RuntimeException e) {
            LOG.warn("Failed to execute query.", e);
            // TODO mark error
            clearTable();
        }

    }

    @Override
    public DataQuery getQuery() {
        return query;
    }

    public TableAutomatonInfo.QueryAutomatonInfo getQueryAutomatonInfo() {
        return new TableAutomatonInfo.QueryAutomatonInfo(
                template.getQueryTemplateInfo(),
                new LinkedHashMap<>(parameters),
                new TableAutomatonInfo.QueryInfo(query));
    }

    DefaultQueryTemplate getTemplate() {
        return template;
    }

}
