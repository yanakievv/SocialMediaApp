package com.example.socialmediaappv2.contract

interface BaseView<T> {
    fun setPresenter(_presenter: T)
}