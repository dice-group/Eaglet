app.controller("validatorCtrl", function($scope, eagletData) {
    $scope.directInput = "";
    $scope.tab = "directInput";
    $scope.file;
    $scope.errorArray =[];
    $scope.postDirectInput = function(string){
        $scope.tab = 'progress';
        eagletData.postDirectInput(string).then(function(result){
            $scope.errorArray = result.data;
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
        //console.log(response);
        $scope.errorArray = response.data;
        console.log($scope.errorArray);
        $scope.tab = 'result';
    }
    function formatError(json){

    }
    /*
     * Error Typen Text
     * */
    $scope.errorString = function(error) {
        if(error === undefined ||error === null)
            return null
        var errorTag = error.substr(error.indexOf("#")+1, error.length);

        var errorType = {
            'Overlapping':      'The third kind of error involves the presence of two or more annotations that share at least one word',

            'CombinedTagging': 'This is a nontrivial tier of errors wherein consecutive word ' +
            'sequences are marked as separate entities while the word sequences if combined can ' +
            'be annotated to a more specific entity. ' +
            'In Document i.e. “December” and “2012” are two separate consecutive entities which when combined together, “December 2012” ' +
            'are more apt in the context, i.e., link to the most precise resource.',

            'Combined':         'This is a nontrivial tier of errors wherein consecutive word ' +
            'sequences are marked as separate entities while the word sequences if combined can ' +
            'be annotated to a more specific entity. ' +
            'In Document i.e. “December” and “2012” are two separate consecutive entities which when combined together, “December 2012” ' +
            'are more apt in the context, i.e., link to the most precise resource.',

            'LongDesc':         'The first kind of error stands for annotations of sequences of words which might describe the ' +
            'entity they are linked to but do not contain a surface form of the entity ' +
            'For example, in Document 2 of Figure 1, “a team that won Supporters’ ' +
            'Shield in 2014” is linked to dbr: Seattle_Sounders_FC but the marked text is neither equivalent to the surface form ' +
            'of entity nor directly describes the entity.',

            'WrongPos':         'The next kind of error lies in marking a portion of a word in a ' +
            'sequence of words as an entity. Given that the rule 4.1 states that an annotation is only ' +
            'allowed to mark complete words ' +
            'For example, the “foot” in “football” is marked as an ' +
            'entity, hence violating the basic definition of word.',

            'InvalidUriErr':    'This error category comprises annotations with no valid URI, e.g., an empty URI.',


            'DisambiguationUriErr':'This type of errors involves linking an entity to a ' +
            'non-precise resource page (disambiguation page) instead of a single resource. ' +
            'In Document 1, the entity Seattle is annotated with the URI dbr:Seattle_' +
            'disambiguation, which is a disambiguation page that points to the City ' +
            'dbr:Seattle and the team dbr:Seattle_Sounders_FC. In this case, the team is the correct resource and ' +
            'shoud also be chosen as annotation.',

            'OutdatedUriErr':    'In this category, the entity is linked to an outdated ' +
            'resource which no longer exists in any KB. In Document 2, ' +
            '“China” is linked to dbr:People’s_Republic_of_China which no ' +
            'longer exists in the KB but instead has to be updated to dbr:China.'

        };
        console.log(errorType[errorTag]);
        return errorType[errorTag];

    }
});

