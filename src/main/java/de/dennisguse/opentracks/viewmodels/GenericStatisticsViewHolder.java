package de.dennisguse.opentracks.viewmodels;

import android.util.Pair;
import android.view.LayoutInflater;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.DistanceFormatter;
import de.dennisguse.opentracks.data.models.Speed;
import de.dennisguse.opentracks.data.models.SpeedFormatter;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.databinding.StatsGenericItemBinding;
import de.dennisguse.opentracks.sensors.sensorData.SensorDataSet;
import de.dennisguse.opentracks.services.RecordingData;
import de.dennisguse.opentracks.settings.UnitSystem;
import de.dennisguse.opentracks.ui.customRecordingLayout.DataField;
import de.dennisguse.opentracks.util.StringUtils;

public abstract class GenericStatisticsViewHolder extends StatisticViewHolder<StatsGenericItemBinding> {

    @Override
    protected StatsGenericItemBinding createViewBinding(LayoutInflater inflater) {
        return StatsGenericItemBinding.inflate(inflater);
    }

    @Override
    public void configureUI(DataField dataField) {
        getBinding().statsValue.setTextAppearance(dataField.isPrimary() ? R.style.TextAppearance_OpenTracks_PrimaryValue
                : R.style.TextAppearance_OpenTracks_SecondaryValue);
        getBinding().statsDescriptionMain
                .setTextAppearance(dataField.isPrimary() ? R.style.TextAppearance_OpenTracks_PrimaryHeader
                        : R.style.TextAppearance_OpenTracks_SecondaryHeader);
    }

    public static class Distance extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            Pair<String, String> valueAndUnit = DistanceFormatter.Builder()
                    .setUnit(unitSystem)
                    .build(getContext()).getDistanceParts(data.getTrackStatistics().getTotalDistance());

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(getContext().getString(R.string.stats_distance));
        }
    }

    public static class TotalTime extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            Pair<String, String> valueAndUnit = new Pair<>(
                    StringUtils.formatElapsedTime(data.getTrackStatistics().getTotalTime()), null);

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(getContext().getString(R.string.stats_total_time));
        }
    }

    public static class MovingTime extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            String value = StringUtils.formatElapsedTime(data.getTrackStatistics().getMovingTime());

            getBinding().statsValue.setText(value);
            getBinding().statsDescriptionMain.setText(getContext().getString(R.string.stats_moving_time));
        }
    }

    public abstract static class SpeedOrPace extends GenericStatisticsViewHolder {

        private final boolean reportSpeed;

        public SpeedOrPace(boolean reportSpeed) {
            this.reportSpeed = reportSpeed;
        }

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            SpeedFormatter localSpeedFormatter = SpeedFormatter.Builder()
                    .setUnit(unitSystem)
                    .setReportSpeedOrPace(reportSpeed)
                    .build(getContext());

            Pair<String, String> valueAndUnit;

            SensorDataSet sensorDataSet = data.sensorDataSet();
            final TrackPoint latestTrackPoint = data.latestTrackPoint();
            if (sensorDataSet != null && sensorDataSet.getSpeed() != null) {
                valueAndUnit = localSpeedFormatter.getSpeedParts(sensorDataSet.getSpeed().first);
                getBinding().statsDescriptionMain.setText(sensorDataSet.getSpeed().second);
            } else {
                Speed speed = latestTrackPoint != null && latestTrackPoint.hasSpeed() ? latestTrackPoint.getSpeed()
                        : null;
                valueAndUnit = localSpeedFormatter.getSpeedParts(speed);

                String title = reportSpeed ? getContext().getString(R.string.stats_speed)
                        : getContext().getString(R.string.stats_pace);
                getBinding().statsDescriptionMain.setText(title);
            }

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
        }
    }

    public static class SpeedVH extends SpeedOrPace {

        public SpeedVH() {
            super(true);
        }
    }

    public static class PaceVH extends SpeedOrPace {

        public PaceVH() {
            super(false);
        }
    }

    public abstract static class SpeedStatisticViewHolder extends GenericStatisticsViewHolder {
        private final boolean reportSpeed;
        private final int textResId;

        public SpeedStatisticViewHolder(boolean reportSpeed, int textResId) {
            this.reportSpeed = reportSpeed;
            this.textResId = textResId;
        }

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            SpeedFormatter speedFormatter = SpeedFormatter.Builder()
                    .setUnit(unitSystem)
                    .setReportSpeedOrPace(reportSpeed)
                    .build(getContext());

            Pair<String, String> valueAndUnit = speedFormatter.getSpeedParts(getSpeedValue(data));

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(getContext().getString(textResId));
        }

        protected abstract Speed getSpeedValue(RecordingData data);
    }

    public static class AverageMovingSpeed extends SpeedStatisticViewHolder {
        public AverageMovingSpeed() {
            super(true, R.string.stats_average_moving_speed);
        }

        @Override
        protected Speed getSpeedValue(RecordingData data) {
            return data.getTrackStatistics().getAverageMovingSpeed();
        }
    }

    public static class AverageSpeed extends SpeedStatisticViewHolder {
        public AverageSpeed() {
            super(true, R.string.stats_average_speed);
        }

        @Override
        protected Speed getSpeedValue(RecordingData data) {
            return data.getTrackStatistics().getAverageSpeed();
        }
    }

    public static class MaxSpeed extends SpeedStatisticViewHolder {
        public MaxSpeed() {
            super(true, R.string.stats_max_speed);
            // public static class AveragePaceVH extends GenericStatisticsViewHolder {

            // private final boolean isMovingPace;

            // protected AveragePaceVH(boolean isMovingPace) {
            // this.isMovingPace = isMovingPace;
        }

        @Override
        protected Speed getSpeedValue(RecordingData data) {
            return data.getTrackStatistics().getMaxSpeed();
        }
    }

    public static class Pace extends GenericStatisticsViewHolder {
        private final int textResId;

        Pace(int textResId) {
            this.textResId = textResId;
        }

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            SpeedFormatter speedFormatterSpeed = SpeedFormatter.Builder()
                    .setUnit(unitSystem)
                    .setReportSpeedOrPace(false)
                    .build(getContext());

            Pair<String, String> valueAndUnit = speedFormatterSpeed
                    .getSpeedParts(data.getTrackStatistics().getAverageMovingSpeed());

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(getContext().getString(textResId));
        }
    }

    public static class FastestPace extends SpeedStatisticViewHolder {
        public FastestPace() {
            super(false, R.string.stats_fastest_pace);

            // getBinding().statsDescriptionMain.setText(
            // getContext().getString(isMovingPace ? R.string.stats_average_moving_pace :
            // R.string.stats_average_pace)
            // );
            // }
            // }

            // // Subclasses for map-based instantiation
            // public static class AverageMovingPace extends AveragePaceVH {
            // public AverageMovingPace() {
            // super(true);
            // }
            // }

            // public static class AveragePace extends AveragePaceVH {
            // public AveragePace() {
            super(false);
        }

        @Override
        protected Speed getSpeedValue(RecordingData data) {
            return data.getTrackStatistics().getMaxSpeed();
        }
    }

    public static class Altitude extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            TrackPoint latestTrackPoint = data.latestTrackPoint();
            Float altitude = latestTrackPoint != null && latestTrackPoint.hasAltitude()
                    ? (float) latestTrackPoint.getAltitude().toM()
                    : null;
            String altitudeReference = latestTrackPoint != null && latestTrackPoint.hasAltitude()
                    ? getContext().getString(latestTrackPoint.getAltitude().getLabelId())
                    : null;
            Pair<String, String> valueAndUnit = StringUtils.getAltitudeParts(getContext(), altitude, unitSystem);

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(R.string.stats_altitude);
            getBinding().statsDescriptionSecondary.setText(altitudeReference);
        }
    }

    public static class Gain extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {

            Pair<String, String> valueAndUnit = StringUtils.getAltitudeParts(getContext(),
                    data.getTrackStatistics().getTotalAltitudeGain(), unitSystem);

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(R.string.stats_gain);
        }
    }

    public static class Loss extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {

            Pair<String, String> valueAndUnit = StringUtils.getAltitudeParts(getContext(),
                    data.getTrackStatistics().getTotalAltitudeLoss(), unitSystem);

            getBinding().statsValue.setText(valueAndUnit.first);
            getBinding().statsUnit.setText(valueAndUnit.second);
            getBinding().statsDescriptionMain.setText(R.string.stats_loss);
        }
    }

    public static class Coordinates extends GenericStatisticsViewHolder {

        @Override
        public void onChanged(UnitSystem unitSystem, RecordingData data) {
            TrackPoint latestTrackPoint = data.latestTrackPoint();
            String value;
            if (latestTrackPoint != null && latestTrackPoint.hasLocation()) {
                value = StringUtils.formatCoordinate(getContext(), latestTrackPoint.getLatitude(),
                        latestTrackPoint.getLongitude());
            } else {
                value = getContext().getString(R.string.value_unknown);
            }

            getBinding().statsValue.setText(value);
            getBinding().statsDescriptionMain.setText(R.string.stats_coordinates);
        }
    }
}
