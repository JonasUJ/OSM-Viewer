package collections.spatial;

public class LinearSearchTwoDTreeTest implements TwoDTreeTest<LinearSearchTwoDTree<String>> {
    @Override
    public LinearSearchTwoDTree<String> createTree() {
        return new LinearSearchTwoDTree<>(2);
    }
}
