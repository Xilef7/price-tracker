package com.xilef7.db

class TooManyAttemptsException(attempts: Int) : RuntimeException("Too many attempts ($attempts)")
