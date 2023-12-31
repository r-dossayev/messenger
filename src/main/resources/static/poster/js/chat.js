$(document).ready(function () {
    loadChats();
    $('#send_form').submit(function (event) {
        event.preventDefault(); // for prevent reload

        let form = $(this);
        let url = form.attr('action');
        let message = form.find('input[name="message"]').val();

        $.post(url, {message: message})
            .done(function (response) {
                // handle success
                loadChats();
                form.find('input[name="message"]').val("");
                console.log("success send message");
            })
            .fail(function (xhr, status, error) {
                // handle error
                console.error(error);
            });
    });
});
const loadChats = () => {
    let url = window.location.href; // Получаем текущий URL страницы
    let segments = url.split("/"); // Разбиваем URL на сегменты по символу "/"
    let username = segments[segments.length - 1]; // Получаем последний сегмент пути URL (в данном случае "John")
    let messagesContent = $("#messages-content");
    // Выполняем AJAX-запрос к REST API для получения сообщений
    $.get("/api/v1/chat/" + username)
        .done(function (response) {

            const chats = response.chats;
            let messagesData = "";
            for (let i = 0; i < chats.length; i++) {
                let chat = chats[i];
                let messageItemClass = (chat.toUser.id === response.currentUserId) ? "message-item" : "message-item outgoing-message";
                let avatarImageSrc = "/defaults/default-user.png"
                if (chat.fromUser.avatar !== null) {
                    avatarImageSrc = chat.fromUser.avatar;
                }
                let createdAt = difForHumans(chat.createdAt);
                let messageItemHtml =
                    '<div class="' + messageItemClass + '">' +
                    '<div class="message-user">' +
                    '<figure class="avatar d-flex">' +
                    '<img src="' + avatarImageSrc + '" alt="image">' +
                    '<div class="message-wrap">' +
                    '<span>' + chat.message + '</span>' +
                    '<p class="time text-end">'+createdAt+'</p>' +
                    '</div>' +
                    '</figure>' +
                    '</div>' +
                    '</div>';
                messagesData += messageItemHtml;
            }
            //append response to messagesContent
            messagesContent.html(messagesData);
            // bottom scroll
            messagesContent.scrollTop(messagesContent.prop("scrollHeight"));
            console.log(messagesContent.prop("scrollHeight"));
        }).fail(function (xhr, status, error) {
        // handle error
        console.error(error);
    });
    function difForHumans(createdAt) {
        const now = new Date();

        const seconds = Math.floor((now - new Date(createdAt)) / 1000);

        if (seconds < 60) {
            return seconds + " s ago";
        } else if (seconds < 3600) {
            const minutes = Math.floor(seconds / 60);
            return minutes + " m ago";
        } else if (seconds < 86400) {
            const hours = Math.floor(seconds / 3600);
            return hours + " h ago";
        } else {
            const days = Math.floor(seconds / 86400);
            return days + " d ago";
        }
    }
}

