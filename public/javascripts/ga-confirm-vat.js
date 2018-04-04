$(document).ready($(function () {

    var form = $("form");

    form.on('submit', on_submit_function = function (e) {
        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (typeof ga === "function") {
            e.preventDefault();
            ga('send', 'event', 'mtdvat', {
                'eventAction': 'confirm vat number',
                'hitCallback' : function () {
                    form.off('submit', on_submit_function);
                    form.submit();
                }
            });
        }
    });


}));
