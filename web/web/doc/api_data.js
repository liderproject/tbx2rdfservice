define({ "api": [
  {
    "type": "post",
    "url": "/tbx2rdf",
    "title": "tbx2rdf",
    "name": "_tbx2rdf",
    "group": "TBX2RDF",
    "version": "1.0.0",
    "description": "<p>Translates a TBX document into a RDF version</p> ",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "namespace",
            "description": "<p>URI of the namespace to be added to the locally generated entities.</p> "
          },
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "action",
            "description": "<p>Action to be made over the input TBX document. Valid values are:</p> <ul> <li>translate</li> Merely makes the translation <li>enrich</li> Makes the translation and enriches the document with links to other terminologies (TEST) <li>reverse</li> Makes the reverse transformation from RDF to TBX (TEST) </ul>"
          },
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "lenient",
            "description": "<p>Determines whether strict or lax parsing is performed. Valid values: true, false (TESTING)</p> "
          },
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "mappings",
            "description": "<p>Mappings to be used, according to the documenation <a href=\"#\">here</a></p> "
          },
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "body",
            "description": "<p>The HTTP message contains the TBX document</p> "
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "RDF",
            "description": "<p>Version of the input data <br/></p> "
          }
        ]
      }
    },
    "filename": "./web/src/java/tbx2rdfservice/servlets/Tbx2rdfServlet.java",
    "groupTitle": "TBX2RDF"
  }
] });