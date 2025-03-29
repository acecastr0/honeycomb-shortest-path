import java.math.BigDecimal;
import java.math.RoundingMode;

public class Coordinates {

    private BigDecimal x;
    private BigDecimal y;

    public Coordinates(BigDecimal x, BigDecimal y) {
        this.x = x.setScale(4, RoundingMode.HALF_UP);
        this.y = y.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setX(double x) {
        this.x = new BigDecimal(x);
    }

    public void setY(double y) {
        this.y = new BigDecimal(y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coordinates) {
            Coordinates other = (Coordinates) obj;
            return this.x.equals(other.x) && this.y.equals(other.y);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return x.hashCode() + y.hashCode();
    }

}
