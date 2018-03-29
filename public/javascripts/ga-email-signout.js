$(document).ready($(function () {

    var link = $("#sign-out");

    link.on('click', on_click_function = function (e) {
        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (typeof ga === "function") {
            e.preventDefault();
            ga('send', 'event', 'mtdvat', {
                'eventAction': 'email sign out',
                'hitCallback' : function () {
                    window.location = link.attr("href");
                }
            });
        }
    });

}));
