package org.mdpnp.apps.testapp;

import javafx.util.Callback;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.mdpnp.apps.testapp.IceApplicationProvider.AppType;

public class DeviceCellFactory implements Callback<GridView<Device>,GridCell<Device>> {

    @Override
    public GridCell<Device> call(GridView<Device> param) {
        return new DeviceGridCell();
    }
}
