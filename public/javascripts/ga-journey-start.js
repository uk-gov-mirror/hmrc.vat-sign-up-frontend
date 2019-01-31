$(document).ready($(function () {

    var form = $('form');
    var BUSINESS_ENTITY_OTHER = 'other';

    form.submit(function() {
        var businessEntity = $('input[type="radio"]:checked').val();
        var eventAction = (businessEntity === BUSINESS_ENTITY_OTHER) ? 'Other entity selected' : 'journey-start';

        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (typeof ga === 'function' && typeof businessEntity !== 'undefined') {
            ga('send', 'event', 'mtdvat', {
                'eventAction': eventAction,
                'eventLabel': businessEntity,
                'hitCallback': function () {
                }
            });
        }
    });

}));
