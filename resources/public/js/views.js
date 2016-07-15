function addHeaders(){
  var headerNameInput = document.getElementById('header-name-input');
  var headerValueInput = document.getElementById('header-value-input');
  var headerNameClone = headerNameInput.cloneNode(true);
  var headerValueClone = headerValueInput.cloneNode(true);
  var emptyDiv = document.createElement('div')

  emptyDiv.setAttribute('class', 'mdl-cell mdl-cell--4-col');

  document.getElementById('bin-response-headers').appendChild(headerNameClone);
  document.getElementById('bin-response-headers').appendChild(headerValueClone);
  document.getElementById('bin-response-headers').appendChild(emptyDiv);}
