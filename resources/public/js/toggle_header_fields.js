(function(window) {

  function cloneByClass(className) {
    return document.querySelector(className)
      .cloneNode(true);
  }

  function appendNewHeaderField(headerField) {
    var form = document.querySelector(".header-form");
    form.appendChild(headerField);
  }

  function removeHeaders() {
    var headerFields = document.querySelectorAll(".bin-response-headers"),
      toBeRemoved = headerFields[headerFields.length-1];

    if (headerFields.length > 1) {
      toBeRemoved.parentNode.removeChild(toBeRemoved);
    }
  }

  function addHeaders() {
    var headerField = cloneByClass(".bin-response-headers"),
      buttons = headerField.querySelectorAll(".header-buttons"),
      emptyDiv = document.createElement("div");

    emptyDiv.setAttribute("class", "mdl-cell mdl-cell--4-col");

    buttons.forEach(function (button) {
      headerField.removeChild(button);
    });

    headerField.appendChild(emptyDiv);
    appendNewHeaderField(headerField);
  }

  function toggleHeaderFields() {
    var addHeaderButton = document.getElementById("add-headers-button");
    var removeHeaderButton = document.getElementById("remove-headers-button");

    if (addHeaderButton) {
      addHeaderButton.addEventListener("click", addHeaders);
    }

    if (removeHeaderButton) {
      removeHeaderButton.addEventListener("click", removeHeaders);
    }
  }

  window.addEventListener("load", toggleHeaderFields, false);

})(window);
