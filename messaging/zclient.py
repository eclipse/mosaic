from zmessaging import ZMessaging


def main():
    client = ZMessaging()
    msg = client.receive_data(1000)
    print(msg)


if __name__ == '__main__':
    main()
