
// DetailsView.java --
//
// DetailsView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.HoverParameterInterpreter;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import ecinternal.client.ui.InternalUIFactory;
import ecinternal.client.ui.Templates;

import ecplugins.EC_CloudManager.client.CloudManagerPlaceManager;
import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.model.AxisOptions;
import ecplugins.EC_CloudManager.client.model.Column;
import ecplugins.EC_CloudManager.client.model.Deployment;
import ecplugins.EC_CloudManager.client.model.TableData;
import ecplugins.EC_CloudManager.client.presenter.DetailsPresenter;
import ecplugins.EC_CloudManager.client.ui.GWTCanvasBasedCanvasFactory;
import ecplugins.EC_CloudManager.client.ui.GWTCanvasBasedCanvasLite;

import com.electriccloud.commander.client.util.StringUtil;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM;

import static com.googlecode.gchart.client.GChart.SymbolType.ANCHOR_NORTHWEST;
import static com.googlecode.gchart.client.GChart.SymbolType.LINE;


@SuppressWarnings({"ProtectedField", "PackageVisibleField"})
public class DetailsView
    extends ViewWithUiHandlers<DetailsUiHandlers>
    implements DetailsPresenter.MyView
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls private static final String X_DATE   = "xDate";
    @NonNls private static final String HH_MM_SS = "%02d:%02d:%02d";

    static {
        GChart.setCanvasFactory(new GWTCanvasBasedCanvasFactory());
    }

    static final long SECONDS = 1000;
    static final long MINUTES = 60 * SECONDS;
    static final long HOURS   = 60 * MINUTES;
    static final long DAYS    = 24 * HOURS;

    //~ Instance fields --------------------------------------------------------

    // Provided by UiBinder
    @UiField GChart       m_chart;
    @UiField(provided = true)
    SimplePager           m_pager;
    @UiField(provided = true)
    CellTable<Deployment> m_table;
    @UiField Styles       m_style;
    @UiField Anchor       m_refreshLink;
    @UiField ListBox      m_chartRange;
    @UiField DeckPanel    m_deckPanel;

    //
    private final Widget                       m_widget;
    private final ListDataProvider<Deployment> m_usageData;

    //~ Constructors -----------------------------------------------------------

    @Inject public DetailsView(
            InternalUIFactory              uiFactory,
            Constants                      constants,
            final CloudManagerPlaceManager placeManager,
            final Templates                templates,
            Binder                         uiBinder)
    {
        m_table     = uiFactory.createCellTable(false);
        m_pager     = uiFactory.createPager();
        m_widget    = uiBinder.createAndBindUi(this);
        m_usageData = new ListDataProvider<Deployment>();

        m_table.addColumn(new TextColumn<Deployment>() {
                @Override public String getValue(Deployment deployment)
                {
                    return DateTimeFormat.getFormat(DATE_TIME_MEDIUM)
                                         .format(
                                             new Date(deployment.getStart()));
                }
            }, constants.started());
        m_table.addColumn(new TextColumn<Deployment>() {
                @Override public String getValue(Deployment deployment)
                {
                    return deployment.getHandle();
                }
            }, constants.handle());
        m_table.addColumn(
            new com.google.gwt.user.cellview.client.Column<Deployment, String>(
                new ClickableTextCell()) {
                @Override public String getValue(Deployment deployment)
                {
                    return deployment.getResource();
                }

                @Override public void render(
                        Cell.Context    context,
                        Deployment      object,
                        SafeHtmlBuilder sb)
                {
                    String resourceName = object.getResource();

                    sb.append(
                        templates.hrefActionLink(0,
                            placeManager.generateResourceUrl(resourceName),
                            resourceName));
                }
            }, constants.resource());
        m_table.addColumn(new DeploymentDurationColumn(constants),
            constants.elapsed());
        m_usageData.addDataDisplay(m_table);
        m_pager.setDisplay(m_table);
        m_chart.setClipToPlotArea(true);
        m_chart.setHoverParameterInterpreter(new HoverParameterInterpreter() {
                @Override public String getHoverParameter(
                        String             paramName,
                        GChart.Curve.Point hoveredOver)
                {
                    String result = null;

                    if (X_DATE.equals(paramName)) {
                        Date date = new Date(Math.round(hoveredOver.getX()));

                        result = DateTimeFormat.getFormat(DATE_TIME_MEDIUM)
                                               .format(date);
                    }

                    return result;
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void showLoading(boolean loading)
    {
        m_deckPanel.showWidget(loading
                ? 0
                : 1);
    }

    @Override public void updateChart(TableData data)
    {
        m_chart.setChartTitle(data.getTitle());
        m_chart.getChartTitle()
               .setStyleName(m_style.chartTitle());
        m_chart.clearCurves();
        m_chart.setGridColor("#CCC");

        boolean      first     = true;
        List<Column> columns   = data.getColumns();
        int          numCurves = columns.size() - 1;

        for (Column column : columns) {
            String labelText = StringUtil.capitalize(column.getLabel());

            if (first) {
                first = false;

                HTML html = new HTML(labelText);

                html.setStyleName(m_style.axisLabel());
                m_chart.getXAxis()
                       .setAxisLabel(html);
            }
            else {
                m_chart.addCurve();

                Map<String, Double> curveData = new HashMap<String, Double>();
                double              tension   = 0.1;

                curveData.put(GWTCanvasBasedCanvasLite.TENSION, tension);

                GChart.Curve curve = m_chart.getCurve();

                curve.setLegendLabel(labelText);
                curve.setCurveData(curveData);

                GChart.Symbol symbol = curve.getSymbol();

                symbol.setSymbolType(LINE);
                symbol.setHovertextTemplate(GChart.formatAsHovertext(
                        column.getHovertextTemplate()));

                if (!StringUtil.isEmpty(column.getColor())) {
                    symbol.setBorderColor(column.getColor());
                }

                symbol.setBackgroundColor(symbol.getBorderColor());
                symbol.setHoverAnnotationSymbolType(ANCHOR_NORTHWEST);

                int thickness = 2;

                symbol.setFillThickness(thickness);
                symbol.setWidth(thickness);
                symbol.setHeight(thickness);
                symbol.setBrushHeight(1);
                symbol.setHoverLocation(GChart.AnnotationLocation.NORTHEAST);
                symbol.setHoverXShift(2);
                symbol.setHoverYShift(2);
                symbol.setHoverSelectionBorderWidth(1);
                symbol.setHoverSelectionHeight(10);
                symbol.setHoverSelectionWidth(10);
                symbol.setHoverSelectionSymbolType(
                    GChart.SymbolType.PIE_SLICE_OPTIMAL_SHADING);
                symbol.setHoverSelectionBackgroundColor(
                    "rgba(255,128,128,0.5)");
                symbol.setHoverSelectionBorderColor("rgba(0,0,0,1)");

                // tall brush thick enough to at least touch 1 point.
                symbol.setBrushSize(40, 40);

                // brush south of mouse ==> selects points below it
                symbol.setBrushLocation(GChart.AnnotationLocation.SOUTH);

                // x-closeness main criterion, but y can still break ties
                symbol.setDistanceMetric(10, 1);
            }
        }

        for (int row = 0; row < data.getRowCount(); ++row) {
            double x = data.getValue(row, 0);

            for (int i = 0; i < numCurves; ++i) {
                double y = data.getValue(row, i + 1);

                m_chart.getCurve(i)
                       .addPoint(x, y);
            }
        }

        setAxisOptions(m_chart.getXAxis(), data.getXAxisOptions());
        setAxisOptions(m_chart.getYAxis(), data.getYAxisOptions());

        // Find a natural size for the chart by looking for an ancestor with
        // a non-zero size
        int width = data.getXChartSize();

        for (Widget w = m_chart.getParent(); w != null; w = w.getParent()) {

            if (w.getOffsetWidth() > 0) {
                width = Math.max(width, w.getOffsetWidth());

                break;
            }
        }

        int xChartSize = Math.max(width
                    - (m_chart.getXChartSizeDecorated()
                        - m_chart.getXChartSize()), data.getXChartSize());

        m_chart.setXChartSize(xChartSize);
        m_chart.setYChartSize(data.getYChartSize());
        m_chart.update();
        showLoading(false);
    }

    @UiHandler("m_refreshLink")
    void handleRefresh(ClickEvent event)
    {
        getUiHandlers().refreshChart();
    }

    @Override public HasChangeHandlers getChartRange()
    {
        return m_chartRange;
    }

    @Override public String getChartRangeValue()
    {
        return m_chartRange.getValue(m_chartRange.getSelectedIndex());
    }

    @Override public ListDataProvider<Deployment> getDeployments()
    {
        return m_usageData;
    }

    private void setAxisOptions(
            GChart.Axis axis,
            AxisOptions options)
    {
        axis.setAxisMin(options.getAxisMin());
        axis.setAxisMax(options.getAxisMax());
        axis.setTickCount(options.getTickCount());
        axis.setHasGridlines(options.getHasGridLines());

        String tickLabelFormat = options.getTickLabelFormat();

        if (!StringUtil.isEmpty(tickLabelFormat)) {
            axis.setTickLabelFormat(tickLabelFormat);
        }
    }

    //~ Inner Interfaces -------------------------------------------------------

    @SuppressWarnings({"InterfaceNeverImplemented", "MarkerInterface"})
    public interface Binder
        extends UiBinder<Widget, DetailsView> { }

    @SuppressWarnings({"GwtCssResourceErrors", "InterfaceNeverImplemented"})
    interface Styles
        extends CssResource
    {

        //~ Methods ------------------------------------------------------------

        String axisLabel();

        String chartTitle();
    }

    //~ Inner Classes ----------------------------------------------------------

    @SuppressWarnings({"PackageVisibleInnerClass"})
    static class DeploymentDurationColumn
        extends TextColumn<Deployment>
    {

        //~ Instance fields ----------------------------------------------------

        private final Constants m_constants;

        //~ Constructors -------------------------------------------------------

        DeploymentDurationColumn(Constants constants)
        {
            m_constants = constants;
        }

        //~ Methods ------------------------------------------------------------

        @Override public String getValue(Deployment deployment)
        {
            long start   = deployment.getStart();
            long now     = new Date().getTime();
            long elapsed = Math.max(0, now - start);
            long days    = elapsed / DAYS;

            if (days > 0) {
                return m_constants.dhmsFormat(days, elapsed % DAYS / HOURS,
                    elapsed % HOURS / MINUTES, elapsed % MINUTES / SECONDS);
            }
            else {
                return m_constants.hmsFormat(elapsed / HOURS,
                    elapsed % HOURS / MINUTES, elapsed % MINUTES / SECONDS);
            }
        }
    }
}
