# suite-scala-sdk ![badge](https://codeship.com/projects/42e2e3d0-559f-0134-b65a-1e6b697efd61/status?branch=master)
Scala sdk for suite

## Implemented endpoints

### Contact Fields

#### Listing Available Fields
GET /api.emarsys.net/api/v2/field -> [ContactFieldApi.list](http://documentation.emarsys.com/resource/developers/endpoints/contacts/list-fields/)

##### Listing predict fields
ContactFieldApi.listPredictFields

### Contacts

#### Querying Contact Data
POST /api/v2/contact/getdata -> [ContactApi.getData](http://documentation.emarsys.com/resource/developers/endpoints/contacts/contact-data/)

### Segments

#### Creating a Segment
PUT /api.emarsys.net/api/v2/filter -> [SegmentApi.create](http://documentation.emarsys.com/resource/developers/endpoints/contacts/creating-a-segment/)