(function(window) {
  window.onload = function() {

    function cloneById(id) {
      return document.getElementById(id)
      .cloneNode(true);
    }

    function appendToResponseHeaders(elements) {
      var headers = document.getElementById('bin-response-headers');

      elements.forEach(function(element) {
        headers.appendChild(element);
      });
    }

    function addHeaders(){
      var headerNameInput = cloneById('header-name-input'),
          headerValueInput = cloneById('header-value-input'),
          emptyDiv = document.createElement('div');

          emptyDiv.setAttribute('class', 'mdl-cell mdl-cell--4-col');

          appendToResponseHeaders([headerNameInput, headerValueInput, emptyDiv]);
    }

    var addHeaderButton = document.getElementById("add-headers-button");
    if (addHeaderButton) {
      addHeaderButton.addEventListener('click', addHeaders);
    }
  }
})(window)
