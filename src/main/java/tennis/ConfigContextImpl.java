package tennis;

public class ConfigContextImpl implements ConfigContext {
    private DataSource dataSource;

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
