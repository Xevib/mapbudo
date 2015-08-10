package com.osm.mapbudo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class EventOverlay  extends Overlay {
    Long starttime;
    private MapbudoEventsReceiver mReceiver;

    public EventOverlay(Context ctx, MapbudoEventsReceiver receiver) {
        super(ctx);
        this.mReceiver = receiver;
    }

    protected void draw(Canvas c, MapView osmv, boolean shadow) {
    }


    public boolean onDown(final MotionEvent e, final MapView mapView) {
        return false;
    }
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView)
    {

        if (event.getActionMasked() == 0)
        {
            this.starttime= System.currentTimeMillis();

        }
        if ((event.getActionMasked()==2) &&((System.currentTimeMillis()-starttime)>500))
        {
            Projection proj = mapView.getProjection();
            GeoPoint p = (GeoPoint)proj.fromPixels((int)event.getX(), (int)event.getY());
            return mReceiver.onLongPressDown(p);
        }
        if ((event.getActionMasked()==1) &&((System.currentTimeMillis()-starttime)>500))
        {
            Projection proj = mapView.getProjection();
            GeoPoint p = (GeoPoint)proj.fromPixels((int)event.getX(), (int)event.getY());
            return mReceiver.onLongPressRelease(p);
        }
        return  false;
    }
    public boolean onKeyUp(final MotionEvent e, final MapView mapView) {
        return false;
    }
    /*public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
        GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
        return this.mReceiver.longPressHelper(p);
    }*/
}