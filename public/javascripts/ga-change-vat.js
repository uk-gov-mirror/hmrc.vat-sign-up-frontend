$(document).ready($(function () {

    var link = $("#sign-in-with-different-details");

    link.on('click', on_click_function = function (e) {
        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (typeof ga === "function") {
            e.preventDefault();
            ga('send', 'event', 'mtdvat', {
                'eventAction': 'change vat number clicked',
                'hitCallback' : function () {
                    window.location = link.attr("href");
                }
            });
        }
    });


}));
