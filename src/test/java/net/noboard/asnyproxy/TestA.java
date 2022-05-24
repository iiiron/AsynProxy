package net.noboard.asnyproxy;

public class TestA implements Test{
    private String name;

    public TestA() {
    }

    public TestA(String name) {
        this.name = name;
    }

    public String getName() throws TestException {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
