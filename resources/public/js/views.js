function addHeaders(){

  var cloneById = function(id) {
    return document.getElementById(id)
      .cloneNode(true);
  };

  var appendToResponseHeaders = function(elements) {
    var headers = document.getElementById('bin-response-headers');

    elements.forEach(function(element) {
      headers.appendChild(element);
    });
  };


  var headerNameInput = cloneById('header-name-input'),
    headerValueInput = cloneById('header-value-input'),
    emptyDiv = document.createElement('div');

  emptyDiv.setAttribute('class', 'mdl-cell mdl-cell--4-col');

  appendToResponseHeaders([headerNameInput, headerValueInput, emptyDiv]);
}

