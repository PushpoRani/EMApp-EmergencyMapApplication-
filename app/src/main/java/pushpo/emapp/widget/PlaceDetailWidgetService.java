package pushpo.emapp.widget;


import android.content.Intent;
import android.widget.RemoteViewsService;

public class PlaceDetailWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PlaceDetailWidgetAdapter(this);
    }
}
