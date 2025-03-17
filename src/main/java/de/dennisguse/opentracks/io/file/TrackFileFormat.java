package de.dennisguse.opentracks.io.file;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.io.file.exporter.CSVTrackExporter;
import de.dennisguse.opentracks.io.file.exporter.GPXTrackExporter;
import de.dennisguse.opentracks.io.file.exporter.KMLTrackExporter;
import de.dennisguse.opentracks.io.file.exporter.KmzTrackExporter;
import de.dennisguse.opentracks.io.file.exporter.TrackExporter;

/**
 * Definition of all possible track formats.
 * <p>
 * NOTE: The names of the entries are used in the user's settings.
 */
public enum TrackFileFormat {

    KML_WITH_TRACKDETAIL_AND_SENSORDATA("KML_WITH_TRACKDETAIL_AND_SENSORDATA") {
        @Override
        public TrackExporter createTrackExporter(@NonNull Context context, @NonNull ContentProviderUtils contentProviderUtils) {
            return new KMLTrackExporter(context, contentProviderUtils, false);
        }

        @Override
        public String getMimeType() {
            return MIME_KML;
        }

        public String getExtension() {
            return "kml";
        }
    },

    @Deprecated //TODO Check if we really need this
    KMZ_WITH_TRACKDETAIL_AND_SENSORDATA("KMZ_WITH_TRACKDETAIL_AND_SENSORDATA", false),

    KMZ_WITH_TRACKDETAIL_AND_SENSORDATA_AND_PICTURES("KMZ_WITH_TRACKDETAIL_AND_SENSORDATA_AND_PICTURES", true),

    GPX("GPX") {
        @Override
        public TrackExporter createTrackExporter(@NonNull Context context, @NonNull ContentProviderUtils contentProviderUtils) {
            return new GPXTrackExporter(contentProviderUtils, context.getString(R.string.app_name));
        }

        @Override
        public String getMimeType() {
            return "application/gpx+xml";
        }

        public String getExtension() {
            return "gpx";
        }
    },

    CSV("CSV") {
        @Override
        public TrackExporter createTrackExporter(@NonNull Context context, @NonNull ContentProviderUtils contentProviderUtils) {
            return new CSVTrackExporter(contentProviderUtils);
        }

        @Override
        public String getMimeType() {
            return "text/csv";
        }

        @Override
        public String getExtension() {
            return "csv";
        }
    };

    private static final String MIME_KMZ = "application/vnd.google-earth.kmz";
    private static final String MIME_KML = "application/vnd.google-earth.kml+xml";
    
    private final String preferenceId;
    private final boolean exportPhotos;

    TrackFileFormat(String preferenceId) {
        this(preferenceId, false);
    }

    TrackFileFormat(String preferenceId, boolean exportPhotos) {
        this.preferenceId = preferenceId;
        this.exportPhotos = exportPhotos;
    }

    public TrackExporter createTrackExporter(@NonNull Context context, @NonNull ContentProviderUtils contentProviderUtils) {
        KMLTrackExporter exporter = new KMLTrackExporter(context, contentProviderUtils, exportPhotos);
        return new KmzTrackExporter(context, contentProviderUtils, exporter, exportPhotos);
    }


    public String getMimeType() {
        return MIME_KMZ;
    }

    public String getExtension() {
        return "kmz";
    }

 
    public boolean includesPhotos() {
        return exportPhotos;
    }

    public static Map<String, String> toPreferenceIdLabelMap(final Resources resources, final TrackFileFormat ... trackFileFormats) {
        Map<String, String> preferenceIdLabelMap = new LinkedHashMap<>(trackFileFormats.length);
        for (TrackFileFormat trackFileFormat : trackFileFormats) {
            String trackFileFormatUpperCase = trackFileFormat.getExtension().toUpperCase(Locale.US); //ASCII upper case
            int photoMessageId = trackFileFormat.includesPhotos() ? R.string.export_with_photos : R.string.export_without_photos;
            preferenceIdLabelMap.put(trackFileFormat.getPreferenceId(), String.format("%s (%s)", trackFileFormatUpperCase, resources.getString(photoMessageId)));
        }
        return preferenceIdLabelMap;
    }

    public static TrackFileFormat valueOfPreferenceId(final String preferenceId) {
        return Arrays.stream(values())
                .filter(trackFileFormat -> trackFileFormat.getPreferenceId().equals(preferenceId))
                .findFirst()
                .orElse(null);
    }

    /**
     * The identifier to be stored in the preferences.
     */
    public String getPreferenceId() {
        return preferenceId;
    }
}