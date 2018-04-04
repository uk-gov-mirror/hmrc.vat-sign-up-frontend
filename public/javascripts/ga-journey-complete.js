$(document).ready($(function () {

    $('[data-entity-type]').each(function () {
        // only try submit to ga if ga token is set, otherwise the ga call will fail and we'll never be able to submit
        if (typeof ga === "function") {
            var entityType = $(this).attr('data-entity-type');
            ga('send', 'event', 'mtdvat', 'journey-complete', entityType);
        }
    });

}));

