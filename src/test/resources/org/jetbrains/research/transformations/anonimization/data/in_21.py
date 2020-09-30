a = 5


def __foo(a):
    a = a + 5
    b = 16
    print(__foo)
    print(__foo())

    def fooE(e):
        pass
    pass


b = 6
a = 6


class A:

    def fooA(self, a):
        pass


    class B:

        def fooB(self, b):
            pass

        @classmethod
        def fooC(cls, b):

            def fooD(z):
                pass
            pass

    def fooZ(self, q):
        pass
    pass