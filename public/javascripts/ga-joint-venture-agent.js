$(document).ready($(function () {

    var form = $("form");

    form.on('submit', on_submit_function = function (e) {
        var jointVentureSelection = $('input[type="radio"]:checked').val();
        var errorMsg = 'No option selected'
        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (typeof ga === "function") {
            e.preventDefault();
            ga('send', 'event', 'mtdvat', {
                'eventAction': 'agent joint venture selection',
                'eventLabel': jointVentureSelection || errorMsg,
                'hitCallback' : function () {
                    form.off('submit', on_submit_function);
                    form.submit();
                }
            });
        }
    });

}));
