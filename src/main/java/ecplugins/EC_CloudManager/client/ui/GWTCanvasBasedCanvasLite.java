
// GWTCanvasBasedCanvasLite.java --
//
// GWTCanvasBasedCanvasLite.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

// Portions of this package are copied from the GChart example

package ecplugins.EC_CloudManager.client.ui;

import java.util.Map;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.GChartCanvasLite;

/**
 * Wrapper around a GWT Canvas to provide basic line drawing facilitities for
 * GChart.
 * <p>
 *
 * GChart does not directly support curvy connecting lines (Bezier
 * curves) even though the GWTCanvas vector graphics library does. So,
 * to get them, we had to override the GChartCanvasLite <tt>lineTo</tt>
 * method so that it instead invokes <tt>cubicCurveTo</tt> appropriately.
 * Essentially, this override treats the lineTo method as if it
 * were a more generic 'curveTo': a request to connect up successive
 * points with a generic continuous connecting line.
 * <p>
 *
 * Bezier curve control points are computed using a very popular and
 * very simple (that's why I chose it) algorithm called Catmull-Rom.
 * Cristopher Twigg wrote this <a
 * href="http://www.cs.cmu.edu/afs/cs/academic/class/15462-f08/www/projects/assn2/assn2/catmullRom.pdf">
 * very clear Catmull-Rom description</tt> that has most of what you
 * need to know about Catmull-Rom to understand this code.
 *
 * <p>
 *
 * Here's the rest of the story: Twigg's cubic interpolation equation
 * for Catmull-Rom is expressed in terms tension (Tau), plus the four
 * points on the curve surrounding (two before, two after) each
 * interpolation interval.  Cubic Bezier curves are expressed in terms
 * of the two points that immediately bracket the interpolation interval
 * and two non-data 'control points' that generally are placed roughly
 * between these point (and usually <i>do not</i> fall on the
 * interpolated curve) using the following interpolation formula:
 *
 *  P(u) = P0*(1-u)^3 + 3*P1*(1-u)^2*u + 3*P2*(1-u)*u^2 + P3*u^3
 *
 * Here u varies from 0 to 1 to produce the interpolated points, and
 * the P0, and P3 are real data points at the end-points of the
 * interpolated curve, and P1, P2 are the control points,
 * (all of these points are 2-D, (x,y) vectors).
 * <p>
 *
 * Twigg's equations are also cubic in u, but are expressed in terms of
 * the four real data points surrounding the interpolated curve
 * segments: p(i-2), p(i-1), p(i), p(i+1) plus an additional parameter
 * Tau (tension) that, roughly, defines how curvy the lines are. The
 * Bezier endpoints, P0 and P3 are equal to Twigg's interpolated curve
 * segment start/end points: p(i-1) and p(i). So we just need a formula
 * for P1 and P2 in terms of Twigg's bracketing data points and Tau.  If
 * you expand the equation above and collect all terms linear in u and
 * then equate the coeeficient on this linear term (3*(-P0+P1)) with the
 * coefficient on Twigg's linear-in-u term (Tau*(p(i)-p(i-2)) you can
 * solve for P1 in terms of just known points in the data sequence and
 * Tau. Equating the quadratic-in-u terms leads to a similar equation
 * for P2 (the P2 equation more simply follows from symmetry with the P1
 * equation).  The resulting equations for all the Bezier Ps are: <p>
 *
 *<pre>
 *      P0 = p(i-1)
 *      P1 = p(i-1) + Tau/3 * (p(i) - p(i-2))
 *      P2 = p(i)   - Tau/3 * (p(i+1) - p(i-1))
 *      P3 = p(i)
 *</pre>
 *
 * <p>
 *
 * Since Tau is often chosen as 0.5, this results in the (reasonably
 * well known) "1/6th rule" for choosing Catmull-Rom cubic Bezier
 * control points.  Tau of 0 gives you linear connecting lines (the code
 * below treats this as a special case calling <tt>lineTo</tt> directly
 * for speed). And, as Tau increases, the connecting lines become "more
 * curvy" (no, I don't know how to define that).  Taus greater than 1.0
 * or less than 0 produce strange-looking curves usually best avoided
 * (they are fun to look at, though).<p>
 *
 * For the first interpolation interval on a curve there is no p(i-2),
 * and for the last interval no p(i+1), so we execute
 * <tt>quadraticCurveTo</tt> (which only requires a single control
 * point, not two) in these cases. Exception: in the case of a simple
 * two-point curve neither p(i-2) nor p(i+1) exist and thus no control
 * points can be calculated via the above formulas...so we just use to the
 * ordinary <tt>lineTo</tt> method.  <p>
 *
 * Because the above algorithm requires a single segment lookahead,
 * actual drawing won't begin until the first three points have been
 * seen via moveTo or lineTo invocations. Furthermore, before the curve
 * is closed, stroked, or filled any of these "look-ahead" segments
 * needed to be "flushed out" so they are included in the stroked or
 * filled path. The code below overides stroke, fill, and closePath to
 * assure that this happens.
 *
 * <p>
 *
 * I considered migrating this interpolation code into GChart proper
 * (adding a 'tension' property to the curve, expanding GChartCanvasLite
 * to include cubicCurveTo, etc.) but Googling revealed a mind-boggling
 * number of different ways to choose the control points that govern the
 * interpolation (there are several ways of dealing with the endpoints,
 * plus C2 cubic splines, least squares fitting, etc.). Not one size
 * fits all. So, especially since this is my first ever use of Bezier
 * curves, and my main reason for selecting Catmull-Rom (not a bad
 * reason, BTW) was it was the easiest to understand and code, I thought
 * exposing this code so you can tweak as needed was best. If you want
 * to refine the algorithm (it doesn't handle irregularly spaced points
 * that well) that's great...please post your improvements/alternatives
 * on the GChart issue tracker.
 *
 */

public class GWTCanvasBasedCanvasLite
    extends Widget
    implements GChartCanvasLite
{

    //~ Static fields/initializers ---------------------------------------------

    // The tension (tau) defines the "curvyness" of interpolated lines
    public static final double STRAIGHT_LINE_TENSION = 0;

    // Classic Catmull-Rom tension: a reasonable curvy-line default
    public static final double REASONABLY_CURVY_TENSION = 0.5;

    // more than this much tension doesn't look right (too curvy)
    public static final double MAX_TENSION = 1.0;

    // Controls the 'curviness' of the line
    @NonNls public static final String TENSION = "tension";

    // Retains the latest 4 data points Catmull-Rom algorithm needs
    private static final int    MAX_POINTS      = 4;
    private static final double DEFAULT_TENSION = STRAIGHT_LINE_TENSION;

    //~ Instance fields --------------------------------------------------------

    private final Canvas    m_canvas;
    private final Context2d m_canvasContext;
    private final double[]  m_x    = new double[MAX_POINTS];
    private final double[]  m_y    = new double[MAX_POINTS];
    private int             m_nPts; // number of points actually in buffer now
    private double          m_tau  = DEFAULT_TENSION;

    //~ Constructors -----------------------------------------------------------

    public GWTCanvasBasedCanvasLite()
    {
        m_canvas        = Canvas.createIfSupported();
        m_canvasContext = m_canvas.getContext2d();
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void arc(
            double  x,
            double  y,
            double  radius,
            double  startAngle,
            double  endAngle,
            boolean antiClockwise)
    {
        m_canvasContext.arc(x, y, radius, startAngle, endAngle, antiClockwise);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void beginPath()
    {
        m_canvasContext.beginPath();

        // grab the curve's setCurveData-defined custom data object
        Map<String, Double> curveData = (Map<String, Double>) GChart
                .getCurrentCurveData();

        m_tau  = curveData == null
            ? DEFAULT_TENSION
            : curveData.get(TENSION);
        m_nPts = 0; // clears out rolling point buffer
    }

    @Override public void clear()
    {
        m_canvasContext.clearRect(0, 0, m_canvas.getCoordinateSpaceWidth(),
            m_canvas.getCoordinateSpaceHeight());
    }

    // make sure that any "look-ahead" segments get included
    @Override public void closePath()
    {

        if (m_nPts > 0) {
            flushCurve();
        }

        m_canvasContext.closePath();
    }

    // make sure that any "look-ahead" segments get included
    @Override public void fill()
    {

        if (m_nPts > 0) {
            flushCurve();
        }

        m_canvasContext.fill();
    }

    @Override public void lineTo(
            double xIn,
            double yIn)
    {

        if (m_tau == STRAIGHT_LINE_TENSION) {
            m_canvasContext.lineTo(xIn, yIn);
        }
        else { // curvy connecting lines

            // noinspection FloatingPointEquality
            if (m_nPts == 1
                    && (m_x[m_nPts - 1] == xIn || m_y[m_nPts - 1] == yIn)) {

                // If the very first line is vertical or horizontal,
                // it's likely a "cap-off" edge of an area chart, and
                // such non-data lines should never be curvy. Very
                // unlikely to cause problems with non-area curves.
                flushCurve();
                m_canvasContext.lineTo(xIn, yIn);
                logPoint(xIn, yIn);
            }
            else if (m_nPts == 0 || m_nPts == 1) {

                // not enough points for curvy interplotion yet
                logPoint(xIn, yIn);
            }
            else if (m_nPts == 2) { // first line + next point available
                logPoint(xIn, yIn);

                double P2x = m_x[1] - m_tau / 3 * (m_x[2] - m_x[0]);
                double P2y = m_y[1] - m_tau / 3 * (m_y[2] - m_y[0]);

                m_canvasContext.quadraticCurveTo(P2x, P2y, m_x[1], m_y[1]);
            }
            else if (m_nPts >= 3) { // normal case: 4 bracketing points
                logPoint(xIn, yIn);

                double P1x = m_x[1] + m_tau / 3 * (m_x[2] - m_x[0]);
                double P1y = m_y[1] + m_tau / 3 * (m_y[2] - m_y[0]);
                double P2x = m_x[2] - m_tau / 3 * (m_x[3] - m_x[1]);
                double P2y = m_y[2] - m_tau / 3 * (m_y[3] - m_y[1]);

                m_canvasContext.bezierCurveTo(P1x, P1y, P2x, P2y, m_x[2],
                    m_y[2]);
            }
        }
    }

    @Override public void moveTo(
            double xIn,
            double yIn)
    {

        if (m_tau != STRAIGHT_LINE_TENSION) {
            flushCurve(); // break in continuity
            logPoint(xIn, yIn);
        }

        m_canvasContext.moveTo(xIn, yIn);
    }

    @Override public void resize(
            int width,
            int height)
    {
        m_canvas.setCoordinateSpaceWidth(width);
        m_canvas.setCoordinateSpaceHeight(height);
    }

    // make sure that any "look-ahead" segments get included
    @Override public void stroke()
    {

        if (m_nPts > 0) {
            flushCurve();
        }

        m_canvasContext.stroke();
    }

    // Note: algorithm never has more than 1 final, undrawn, segment
    private void flushCurve()
    {

        if (m_nPts == 2) { // two points only determine a line
            m_canvasContext.lineTo(m_x[m_nPts - 1], m_y[m_nPts - 1]);
        }
        else if (m_nPts >= 3) {

            // If the very last line is vertical or horizontal,
            // it's likely a "cap-off" edge of an area chart, and
            // such non-data lines should never be curvy. Very
            // unlikely to cause problems with non-area curves.
            // noinspection FloatingPointEquality
            if (m_x[m_nPts - 1] == m_x[m_nPts - 2]
                    || m_y[m_nPts - 1] == m_y[m_nPts - 2]) {
                m_canvasContext.lineTo(m_x[m_nPts - 1], m_y[m_nPts - 1]);
            }
            else {
                double P1x = m_x[m_nPts - 2]
                        + m_tau / 3 * (m_x[m_nPts - 1] - m_x[m_nPts - 3]);
                double P1y = m_y[m_nPts - 2]
                        + m_tau / 3 * (m_y[m_nPts - 1] - m_y[m_nPts - 3]);

                m_canvasContext.quadraticCurveTo(P1x, P1y, m_x[m_nPts - 1],
                    m_y[m_nPts - 1]);
            }
        }

        m_nPts = 0; // clears out rolling point buffer
    }

    // remembers a rolling window of the most recent 'lineTo' points
    private void logPoint(
            double xIn,
            double yIn)
    {

        if (m_nPts < MAX_POINTS) { // not yet full buffer
            m_nPts++;
        }
        else {                     // buffer is full, make room for the new
                                   // point

            for (int i = 1; i < MAX_POINTS; i++) {
                m_x[i - 1] = m_x[i];
                m_y[i - 1] = m_y[i];
            }
        }

        m_x[m_nPts - 1] = xIn;
        m_y[m_nPts - 1] = yIn;
    }

    @Override
    @SuppressWarnings({"RefusedBequest"})
    public Element getElement()
    {
        return m_canvas.getElement();
    }

    @Override public void setFillStyle(String cssColor)
    {
        m_canvasContext.setFillStyle(cssColor);
    }

    @Override public void setLineWidth(double width)
    {
        m_canvasContext.setLineWidth(width);
    }

    @Override public void setStrokeStyle(String cssColor)
    {
        m_canvasContext.setLineJoin(Context2d.LineJoin.ROUND);
        m_canvasContext.setStrokeStyle(cssColor);
    }
}
