a = 5


def __foo(a):
    a = a + 5
    b = 16
    print(__foo)
    print(__foo())
    pass


a = 6


class A:

    def fooA(self, a):
        pass


    class __B:

        def __fooB(self, b):
            pass
        pass
    pass