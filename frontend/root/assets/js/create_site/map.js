"use strict";
proj4.defs(
  "ESRI:102696",
  "+proj=tmerc +lat_0=35.8333333333333 +lon_0=-90.5 +k=0.999933333333333 +x_0=250000 +y_0=0 +datum=NAD83 +units=us-ft +no_defs +type=crs"
);
proj4.defs(
  "ESRI:102697",
  "+proj=tmerc +lat_0=35.8333333333333 +lon_0=-92.5 +k=0.999933333333333 +x_0=500000.000000001 +y_0=0 +datum=NAD83 +units=us-ft +no_defs +type=crs"
);
proj4.defs(
  "ESRI:102698",
  "+proj=tmerc +lat_0=36.1666666666667 +lon_0=-94.5 +k=0.999941176470588 +x_0=850000 +y_0=0 +datum=NAD83 +units=us-ft +no_defs +type=crs"
);
proj4.defs("EPSG:4326", "+proj=longlat +datum=WGS84 +no_defs +type=crs");
ol.proj.proj4.register(proj4);

const map = new ol.Map({
  target: "map",
  layers: [
    new ol.layer.Tile({
      source: new ol.source.OSM(),
    }),
  ],
  view: new ol.View({
    //MIN X, Y, MAX X, Y
    showFullExtent: true,
    extent: [
      -10661067.63327181, 4297869.751737669, -9918566.629680675,
      4954985.832353976,
    ],
    center: [-10382285.999155946, 4273342.734820133],
    projection: "EPSG:3857",
    zoom: 1,
  }),
});

const stateLayer = new ol.layer.VectorImage({
  source: new ol.source.Vector({
    url: `/assets/us-state-boundaries.geojson`,
    format: new ol.format.GeoJSON(),
  }),
  visible: true,
  style: new ol.style.Style({
    fill: new ol.style.Stroke({
      color: [0, 0, 255, 0.25],
    }),
  }),
});
const markers = new ol.layer.Vector({
  source: new ol.source.Vector(),
  style: new ol.style.Style({
    image: new ol.style.Icon({
      anchor: [0.5, 1],
      size: [384, 512],
      scale: 0.15,
      src: `/assets/marker.png`,
    }),
  }),
});

map.addLayer(stateLayer);
map.addLayer(markers);

//map.on('postcompose',function(e){
//document.querySelector('canvas').style.filter="invert(90%)";
//});
