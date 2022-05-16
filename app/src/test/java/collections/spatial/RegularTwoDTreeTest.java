package collections.spatial;

public class RegularTwoDTreeTest implements TwoDTreeTest<TwoDTree<String>> {
    @Override
    public TwoDTree<String> createTree() {
        return new TwoDTree<>();
    }
}
