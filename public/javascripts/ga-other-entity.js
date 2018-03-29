$(document).ready($(function () {

    var form = $("form");

    form.on('submit', on_submit_function = function (e) {
        var isOtherEntity = $('#other:checked').size() > 0;

        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (isOtherEntity && typeof ga === "function") {
            e.preventDefault();
            ga('send', 'event', 'mtdvat', {
                'eventAction': 'other entity selected',
                'hitCallback': function () {
                    form.off('submit', on_submit_function);
                    form.submit();
                }
            });
        }
    });

}));
