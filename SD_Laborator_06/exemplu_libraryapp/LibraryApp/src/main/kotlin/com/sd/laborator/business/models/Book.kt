package com.sd.laborator.business.models

class Book(private var data: Content) {

    var id: Int
        get() {
            return data.id
        }
        set(value) {
            data.id = value
        }

    var title: String?
        get() {
            return data.title
        }
        set(value) {
            data.title = value
        }

    var author: String?
        get() {
            return data.author
        }
        set(value) {
            data.author = value
        }

    var publisher: String?
        get() {
            return data.publisher
        }
        set(value) {
            data.publisher = value
        }

    var content: String?
        get() {
            return data.text
        }
        set(value) {
            data.text = value
        }

    fun hasAuthor(author: String): Boolean {
        return data.author.equals(author)
    }

    fun hasTitle(title: String): Boolean {
        return data.title.equals(title)
    }

    fun publishedBy(publisher: String): Boolean {
        return data.publisher.equals(publisher)
    }

}