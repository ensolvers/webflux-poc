# Google Geolocalization

Core support using the Google Geolocalization paid service.
The service: [`GeoLocationService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/GeoLocationService.java) and the controller  [`GeoLocalizationController`](../modules/ensolvers-core-backend-api/src/main/java/com/ensolvers/core/api/controller/GeoLocalizationController.java) supports the following functionality:

- **Get address from Coordinates:** giving the coordinates (latitude and longitude) returns its address. 
- **Get place predictions:** giving an address or place to look for it returns a list of places/addresses. 
- **Get place detail:** giving a place id and a session token returns its address.

Note:
- Get place predictions and get place details work together. They work as a transaction and they need to use the same token. 
- Once you request to get place details, generate a new token for future request.
- Using the same token, you can call get place details several time, and then finally call to get place detail.
- Doing so, you will be charged only once.

Note: You need to set up property: `core.ensolvers.google.map.client-secret` with the private secret.
