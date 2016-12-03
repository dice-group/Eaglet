app.controller("validatorCtrl", function($scope, eagletData) {
    $scope.directInput = "";
    $scope.tab = "directInput";
    $scope.file;
    $scope.exampleJson ={
        "@graph": [
            {
                "@id": "http://example.org/Example2FromThePaper#char=0,157",
                "@type": [
                    "nif:RFC5147String",
                    "nif:String",
                    "nif:Context"
                ],
                "beginIndex": "0",
                "endIndex": "157",
                "nif:isString": "John played football for Seattle after he spent a semester in China. Such a team that won Supporters' Shield in 2014 boosts players like him in their career."
            },
            {
                "@id": "http://example.org/Example2FromThePaper#char=12,16",
                "@type": [
                    "nif:String",
                    "nif:Phrase",
                    "nif:RFC5147String"
                ],
                "hasCheckResult": "http://gerbil.aksw.org/eaglet/vocab#Deleted",
                "hasErrorType": "http://gerbil.aksw.org/eaglet/vocab#WrongPos",
                "nif:anchorOf": "foot",
                "beginIndex": "12",
                "endIndex": "16",
                "referenceContext": "http://example.org/Example2FromThePaper#char=0,157",
                "taIdentRef": "http://dbpedia.org/resource/Foot"
            },
            {
                "@id": "http://example.org/Example2FromThePaper#char=62,67",
                "@type": [
                    "nif:Phrase",
                    "nif:String",
                    "nif:RFC5147String"
                ],
                "hasCheckResult": "http://gerbil.aksw.org/eaglet/vocab#OutdatedUri",
                "hasErrorType": "http://gerbil.aksw.org/eaglet/vocab#OutdatedUriErr",
                "nif:anchorOf": "China",
                "beginIndex": "62",
                "endIndex": "67",
                "referenceContext": "http://example.org/Example2FromThePaper#char=0,157",
                "taIdentRef": "http://dbpedia.org/resource/People's_Republic_of_China"
            },
            {
                "@id": "http://example.org/Example2FromThePaper#char=76,116",
                "@type": [
                    "nif:Phrase",
                    "nif:RFC5147String",
                    "nif:String"
                ],
                "hasCheckResult": "http://gerbil.aksw.org/eaglet/vocab#Deleted",
                "hasErrorType": "http://gerbil.aksw.org/eaglet/vocab#LongDesc",
                "nif:anchorOf": "team that won Supporters' Shield in 2014",
                "beginIndex": "76",
                "endIndex": "116",
                "referenceContext": "http://example.org/Example2FromThePaper#char=0,157",
                "taIdentRef": "http://dbpedia.org/resource/Seattle_Sounders_FC"
            }
        ],
        "@context": {
            "hasErrorType": {
                "@id": "http://gerbil.aksw.org/eaglet/vocab#hasErrorType",
                "@type": "@id"
            },
            "referenceContext": {
                "@id": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext",
                "@type": "@id"
            },
            "beginIndex": {
                "@id": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex",
                "@type": "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
            },
            "hasCheckResult": {
                "@id": "http://gerbil.aksw.org/eaglet/vocab#hasCheckResult",
                "@type": "@id"
            },
            "anchorOf": {
                "@id": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf",
                "@type": "http://www.w3.org/2001/XMLSchema#string"
            },
            "taIdentRef": {
                "@id": "http://www.w3.org/2005/11/its/rdf#taIdentRef",
                "@type": "@id"
            },
            "endIndex": {
                "@id": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex",
                "@type": "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
            },
            "isString": {
                "@id": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString",
                "@type": "http://www.w3.org/2001/XMLSchema#string"
            },
            "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "xsd": "http://www.w3.org/2001/XMLSchema#",
            "itsrdf": "http://www.w3.org/2005/11/its/rdf#",
            "nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
            "rdfs": "http://www.w3.org/2000/01/rdf-schema#"
        }
    };
    $scope.postDirectInput = function(string){

        $scope.tab = 'progress';
        eagletData.postDirectInput(string).then(function(result){
            $scope.exampleJson = result;
            $scope.tab = 'result';
        });
    }
    $scope.doUpload = function () {
        console.log("fileUpload");
        console.log($scope.file);
        $scope.tab = 'progress';
        //multipart file
        eagletData.uploadFile($scope.file);

    }
    $scope.parseResult= function(response){
        console.log(response);
        $scope.exampleJson = response;
        $scope.tab = 'result';
    }
    function formatError(json){

    }
});